#include "group_writer.hpp"
#include "group.hpp"
#include "alloc_slab.hpp"

using namespace tightdb;

// todo, test (int) cast
GroupWriter::GroupWriter(Group& group) :
    m_group(group), m_alloc(group.get_allocator()), m_len(m_alloc.GetFileLen()), m_fd((int)m_alloc.GetFileDescriptor())
{
}

bool GroupWriter::IsValid() const
{
    return m_fd != 0;
}

void GroupWriter::SetVersions(size_t current, size_t readlock) {
    assert(readlock <= current);
    m_current_version  = current;
    m_readlock_version = readlock;
}

void GroupWriter::Commit()
{
    Array& top          = m_group.get_top_array();
    Array& fpositions   = m_group.m_freePositions;
    Array& flengths     = m_group.m_freeLengths;
    Array& fversions    = m_group.m_freeVersions;
    const bool isShared = m_group.is_shared();
    assert(fpositions.Size() == flengths.Size());

    // Recursively write all changed arrays
    // (but not top yet, as it contains refs to free lists which are changing)
    const size_t n_pos = m_group.m_tableNames.Write(*this, true, true);
    const size_t t_pos = m_group.m_tables.Write(*this, true, true);

    // Add free space created during this commit to free lists
    const SlabAlloc::FreeSpace& freeSpace = m_group.get_allocator().GetFreespace();
    const size_t fcount = freeSpace.size();
    for (size_t i = 0; i < fcount; ++i) {
        SlabAlloc::FreeSpace::ConstCursor r = freeSpace[i];
        fpositions.add(r.ref);
        flengths.add(r.size);
        if (isShared) fversions.add(m_current_version);
    }
    //TODO: Consolidate free list

    // We now have a bit of an chicken-and-egg problem. We need to write our free
    // lists to the file, but the act of writing them will affect the amount
    // of free space, changing them.

    // To make sure we have room for top and free list we calculate the absolute
    // largest size they can get:
    // (64bit width + one possible ekstra entry per alloc and header)
    const size_t free_count = fpositions.Size() + 5;
    size_t top_max_size = (5 + 1) * 8;
    size_t flist_max_size = (free_count) * 8;

    // Reserve space for each block. We explicitly ask for a bigger space than
    // the block can occupy, so that we know that we will have to add the rest
    // space later
    const size_t top_pos = m_group.get_free_space(top_max_size, m_len);
    const size_t fp_pos  = m_group.get_free_space(flist_max_size, m_len);
    const size_t fl_pos  = m_group.get_free_space(flist_max_size, m_len);
    const size_t fv_pos  = isShared ? m_group.get_free_space(flist_max_size, m_len) : 0;

    // Update top and make sure that it is big enough to hold any position
    // the free lists can get
    top.Set(0, n_pos);
    top.Set(1, t_pos);
    top.Set(2, m_len); // just to expand width, values for free tracking set later

    // Add dummy values to freelists so we can get the final size.
    // The values are chosen to be so big that we are guaranteed that
    // the list will not expand width when the real values are set later.
    fpositions.add(m_len);
    fpositions.add(m_len);
    fpositions.add(m_len);
    fpositions.add(m_len);
    flengths.add(flist_max_size);
    flengths.add(flist_max_size);
    flengths.add(flist_max_size);
    flengths.add(flist_max_size);
    if (isShared) {
        fversions.add(0);
        fversions.add(0);
        fversions.add(0);
        fversions.add(0);
    }

    // Get final sizes
    const size_t top_size = top.GetByteSize(true);
    const size_t fp_size  = fpositions.GetByteSize(true);
    const size_t fl_size  = flengths.GetByteSize(true);
    const size_t fv_size  = isShared ? flengths.GetByteSize(true) : 0;

    // Set the correct values for rest space
    size_t fc = fpositions.Size()-1;
    if (isShared) {
        fpositions.Set(fc, fv_pos + fv_size);
        flengths.Set(fc--, flist_max_size - fv_size);
    }
    fpositions.Set(fc, fl_pos + fl_size);
    flengths.Set(fc--, flist_max_size - fl_size);
    fpositions.Set(fc, fp_pos + fp_size);
    flengths.Set(fc--, flist_max_size - fp_size);
    fpositions.Set(fc, top_pos + top_size);
    flengths.Set(fc, top_max_size - top_size);

    // Write free lists
    fpositions.WriteAt(fp_pos, *this);
    flengths.WriteAt(fl_pos, *this);
    if (isShared) fversions.WriteAt(fv_pos, *this);

    // Write top
    top.Set(2, fp_pos);
    top.Set(3, fl_pos);
    if (isShared) top.Set(4, fv_pos);
    else if (top.Size() == 5) top.Delete(4); // versions
    top.WriteAt(top_pos, *this);

    // Commit
    DoCommit(top_pos);

    // Clear old allocs
    // and remap if file size has changed
    SlabAlloc& alloc = m_group.get_allocator();
    alloc.FreeAll(m_len);

    // Recusively update refs in all active tables (columns, arrays..)
    m_group.update_refs(top_pos);
}

size_t GroupWriter::write(const char* p, size_t n) {
#if !defined(_MSC_VER) // write persistence
    // Get position of free space to write in (expanding file if needed)
    const size_t pos = m_group.get_free_space(n, m_len);

    // Write the block
    // lseek(m_fd, pos, SEEK_SET);
    // ssize_t r = ::write(m_fd, p, n);
    //static_cast<void>(r); // FIXME: We should probably check for error here!

    // return the position it was written
    return pos;
#endif
    return 0;
}

void GroupWriter::WriteAt(size_t pos, const char* p, size_t n) {
#if !defined(_MSC_VER) // write persistence
    //lseek(m_fd, pos, SEEK_SET);
    //ssize_t r = ::write(m_fd, p, n);
    //static_cast<void>(r); // FIXME: We should probably check for error here!
#endif
}

void GroupWriter::DoCommit(uint64_t topPos)
{
    // In swap-only mode, we just use the file as backing for the shared
    // memory. So we never actually flush the data to disk (the OS may do
    // so for swapping though). Note that this means that the file on disk
    // may very likely be in an invalid state.
    //
    // In async mode, the file is persisted in regular intervals. This means
    // that the file on disk will always be in a valid state, but it may be
    // slightly out of sync with the latest changes.
    //if (isSwapOnly || isAsync) return;

#if !defined(_MSC_VER) // write persistence
    //fsync(m_fd);
    //lseek(m_fd, 0, SEEK_SET);
    //ssize_t r = ::write(m_fd, (const char*)&topPos, 8);
    //static_cast<void>(r); // FIXME: We should probably check for error here!
    //fsync(m_fd); // Could be fdatasync on Linux
#endif
}
