
// Memory Mapping includes
#ifdef _MSC_VER
#include <windows.h>
#include <stdio.h>
#include <conio.h>
#include <stdio.h>
#else
#include <unistd.h> // close()
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/mman.h>
#endif

#include <cassert>
#include <iostream>
#include "alloc_slab.hpp"
#include "array.hpp"

#ifdef _DEBUG
#include <cstdio>
#endif //_DEBUG

using namespace std;

namespace {

using namespace tightdb;

// Support function
// todo, fixme: use header function in array instead!
size_t GetSizeFromHeader(void* p)
{
    // parse the capacity part of 8byte header
    const uint8_t* const header = (uint8_t*)p;
    return (header[4] << 16) + (header[5] << 8) + header[6];
}

}


namespace tightdb {

Allocator& GetDefaultAllocator()
{
    static Allocator DefaultAllocator;
    return DefaultAllocator;
}

SlabAlloc::SlabAlloc(): m_shared(NULL), m_owned(false), m_baseline(8)
{
#ifdef _DEBUG
    m_debugOut = false;
#endif //_DEBUG
}

SlabAlloc::~SlabAlloc()
{
#ifdef _DEBUG
    if (!IsAllFree()) {
        m_slabs.print();
        m_freeSpace.print();
        assert(false);
    }
#endif //_DEBUG

    // Release all allocated memory
    for (size_t i = 0; i < m_slabs.size(); ++i) {
        void* p = (void*)(intptr_t)m_slabs[i].pointer;
        free(p);
    }

    // Release any shared memory
    if (m_shared) {
        if (m_owned) {
            free(m_shared);
        }
        else {
#ifdef _MSC_VER
            UnmapViewOfFile(m_shared);
            CloseHandle(m_fd);
            CloseHandle(m_mapfile);
#else
            munmap(m_shared, m_baseline);
            close(m_fd);
#endif
        }
    }
}

MemRef SlabAlloc::Alloc(size_t size)
{
    assert((size & 0x7) == 0); // only allow sizes that are multibles of 8

    // Do we have a free space we can reuse?
    for (size_t i = 0; i < m_freeSpace.size(); ++i) {
        FreeSpace::Cursor r = m_freeSpace[i];
        if (r.size >= (int)size) {
            const size_t location = (size_t)r.ref;
            const size_t rest = (size_t)r.size - size;

            // Update free list
            if (rest == 0) m_freeSpace.remove(i);
            else {
                r.size = rest;
                r.ref += (unsigned int)size;
            }

#ifdef _DEBUG
            if (m_debugOut) {
                printf("Alloc ref: %lu size: %lu\n", location, size);
            }
#endif //_DEBUG

            void* pointer = Translate(location);
            return MemRef(pointer, location);
        }
    }

    // Else, allocate new slab
    const size_t multible = 256 * ((size / 256) + 1);
    const size_t slabsBack = m_slabs.is_empty() ? m_baseline : m_slabs.back().offset;
    const size_t doubleLast = m_slabs.is_empty() ? 0 :
        (slabsBack - ((m_slabs.size() == 1) ? size_t(0) : m_slabs.back(-2).offset)) * 2;
    const size_t newsize = multible > doubleLast ? multible : doubleLast;

    // Allocate memory
    void* slab = newsize ? malloc(newsize): NULL;
    if (!slab) return MemRef(NULL, 0);

    // Add to slab table
    Slabs::Cursor s = m_slabs.add();
    s.offset = slabsBack + newsize;
    s.pointer = (intptr_t)slab;

    // Update free list
    const size_t rest = newsize - size;
    FreeSpace::Cursor f = m_freeSpace.add();
    f.ref = slabsBack + size;
    f.size = rest;

#ifdef _DEBUG
    if (m_debugOut) {
        printf("Alloc ref: %lu size: %lu\n", slabsBack, size);
    }
#endif //_DEBUG

    return MemRef(slab, slabsBack);
}

void SlabAlloc::Free(size_t ref, void* p)
{
    // Free space in read only segment is tracked separately
    FreeSpace& freeSpace = IsReadOnly(ref) ? m_freeReadOnly : m_freeSpace;

    // Get size from segment
    const size_t size = GetSizeFromHeader(p);
    const size_t refEnd = ref + size;
    bool isMerged = false;

#ifdef _DEBUG
    if (m_debugOut) {
        printf("Free ref: %lu size: %lu\n", ref, size);
    }
#endif //_DEBUG

    // Check if we can merge with start of free block
    const size_t n = freeSpace.cols().ref.find_first(refEnd);
    if (n != (size_t)-1) {
        // No consolidation over slab borders
        if (m_slabs.cols().offset.find_first(refEnd) == (size_t)-1) {
            freeSpace[n].ref = ref;
            freeSpace[n].size += size;
            isMerged = true;
        }
    }

    // Check if we can merge with end of free block
    if (m_slabs.cols().offset.find_first(ref) == (size_t)-1) { // avoid slab borders
        const size_t count = freeSpace.size();
        for (size_t i = 0; i < count; ++i) {
            FreeSpace::Cursor c = freeSpace[i];

        //  printf("%d %d", c.ref, c.size);

            const size_t end = TO_REF(c.ref + c.size);
            if (ref == end) {
                if (isMerged) {
                    c.size += freeSpace[n].size;
                    freeSpace.remove(n);
                }
                else c.size += size;

                return;
            }
        }
    }

    // Else just add to freelist
    if (!isMerged) freeSpace.add(ref, size);
}

MemRef SlabAlloc::ReAlloc(size_t ref, void* p, size_t size)
{
    assert((size & 0x7) == 0); // only allow sizes that are multibles of 8

    //TODO: Check if we can extend current space

    // Allocate new space
    const MemRef space = Alloc(size);
    if (!space.pointer) return space;

    /*if (doCopy) {*/  //TODO: allow realloc without copying
        // Get size of old segment
        const size_t oldsize = GetSizeFromHeader(p);

        // Copy existing segment
        memcpy(space.pointer, p, oldsize);

        // Add old segment to freelist
        Free(ref, p);
    //}

#ifdef _DEBUG
    if (m_debugOut) {
        printf("ReAlloc origref: %lu oldsize: %lu newref: %lu newsize: %lu\n", ref, oldsize, space.ref, size);
    }
#endif //_DEBUG

    return space;
}

void* SlabAlloc::Translate(size_t ref) const
{
    if (ref < m_baseline) return m_shared + ref;
    else {
        const size_t ndx = m_slabs.cols().offset.find_pos(ref);
        assert(ndx != size_t(-1));

        const size_t offset = ndx ? m_slabs[ndx-1].offset : m_baseline;
        return (char*)(intptr_t)m_slabs[ndx].pointer + (ref - offset);
    }
}

bool SlabAlloc::IsReadOnly(size_t ref) const
{
    return ref < m_baseline;
}

bool SlabAlloc::SetSharedBuffer(const char* buffer, size_t len)
{
    // Verify that the topref points to a location within buffer.
    // This is currently the only integrity check we make
    size_t ref = (size_t)(*(uint64_t*)buffer);
    if (ref > len) return false;

    // There is a unit test that calls this function with an invalid buffer
    // so we can't size_t-test range with TO_REF until now
    ref = TO_REF(*(uint64_t*)buffer);
    (void)ref; // the above macro contains an assert, this avoids warning for unused var

    m_shared = (char*)buffer;
    m_baseline = len;
    m_owned = true; // we now own the buffer
    return true;
}

bool SlabAlloc::SetShared(const char* path, bool readOnly)
{
#ifdef _MSC_VER
    assert(readOnly); // write persistence is not implemented for windows yet
    // Open file
    m_fd = CreateFileA(path, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_ALWAYS, NULL, NULL);

    // Map to memory (read only)
    const HANDLE hMapFile = CreateFileMapping(m_fd, NULL, PAGE_WRITECOPY, 0, 0, 0);
    if (hMapFile == NULL || hMapFile == INVALID_HANDLE_VALUE) {
        CloseHandle(m_fd);
        return false;
    }
    const LPCTSTR pBuf = (LPTSTR) MapViewOfFile(hMapFile, FILE_MAP_COPY, 0, 0, 0);
    if (pBuf == NULL) {
        return false;
    }

    // Get Size
    LARGE_INTEGER size;
    GetFileSizeEx(m_fd, &size);
    m_baseline = TO_REF(size.QuadPart);

    m_shared = (char *)pBuf;
    m_mapfile = hMapFile;
#else
    // Open file
    m_fd = open(path, readOnly ? O_RDONLY : O_RDWR|O_CREAT, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
    if (m_fd < 0) return false;

    // Get size
    struct stat statbuf;
    if (fstat(m_fd, &statbuf) < 0) {
        close(m_fd);
        return false;
    }
    size_t len = statbuf.st_size;

    if (readOnly && len == 0) {
        // You have opened an non-existing or empty file
        close(m_fd);
        return false;
    }

    // Handle empty files (new database)
    if (len == 0) {
        if (readOnly) return false;
        ssize_t r = write(m_fd, "\0\0\0\0\0\0\0\0", 8); // write top-ref
	static_cast<void>(r); // FIXME: We should probably check for error here!

        // pre-alloc initial space when mmapping
        len = 1024*1024;
        int r2 = ftruncate(m_fd, len);
        static_cast<void>(r2); // FIXME: We should probably check for error here!
    }

    // Verify that data is 64bit aligned
    if ((len & 0x7) != 0) return false;

    // Map to memory (read only)
    void* const p = mmap(0, len, PROT_READ, MAP_SHARED, m_fd, 0);
    if (p == (void*)-1) {
        close(m_fd);
        return false;
    }

    //TODO: Verify the data structures

    m_shared = (char*)p;
    m_baseline = len;
#endif

    return true;
}

bool SlabAlloc::CanPersist() const
{
    return m_shared != NULL;
}

size_t SlabAlloc::GetTopRef() const
{
    assert(m_shared && m_baseline > 0);

    const size_t ref = TO_REF(*(uint64_t*)m_shared);
    assert(ref < m_baseline);

    return ref;
}

size_t SlabAlloc::GetTotalSize() const
{
    if (m_slabs.is_empty()) {
        return m_baseline;
    }
    else {
        return TO_REF(m_slabs.back().offset);
    }
}

void SlabAlloc::FreeAll(size_t filesize)
{
    assert(filesize >= m_baseline);
    assert((filesize & 0x7) == 0); // 64bit alignment

    // Free all scratch space (done after all data has
    // been commited to persistent space)
    m_freeSpace.clear();
    
    // If the file size have changed, we need to remap the readonly buffer
    ReMap(filesize);

    // Rebuild free list to include all slabs
    size_t ref = m_baseline;
    const size_t count = m_slabs.size();
    for (size_t i = 0; i < count; ++i) {
        const Slabs::Cursor c = m_slabs[i];
        const size_t size = c.offset - ref;

        m_freeSpace.add(ref, size);

        ref = c.offset;
    }
}
   
void SlabAlloc::ReMap(size_t filesize)
{
    assert(m_freeSpace.is_empty());
    
    // If the file size have changed, we need to remap the readonly buffer
    if (filesize == m_baseline) return;
    
    assert(filesize >= m_baseline);
    assert((filesize & 0x7) == 0); // 64bit alignment
    
#if !defined(_MSC_VER) // write persistence
    //void* const p = mremap(m_shared, m_baseline, filesize); // linux only
    munmap(m_shared, m_baseline);
    void* const p = mmap(0, filesize, PROT_READ, MAP_SHARED, m_fd, 0);
    assert(p);
    
    m_shared   = (char*)p;
    m_baseline = filesize;
#endif
}

#ifdef _DEBUG

bool SlabAlloc::IsAllFree() const
{
    if (m_freeSpace.size() != m_slabs.size()) return false;

    // Verify that free space matches slabs
    size_t ref = m_baseline;
    for (size_t i = 0; i < m_slabs.size(); ++i) {
        Slabs::ConstCursor c = m_slabs[i];
        const size_t size = TO_REF(c.offset) - ref;

        const size_t r = m_freeSpace.cols().ref.find_first(ref);
        if (r == (size_t)-1) return false;
        if (size != (size_t)m_freeSpace[r].size) return false;

        ref = TO_REF(c.offset);
    }
    return true;
}

void SlabAlloc::Verify() const
{
    // Make sure that all free blocks fit within a slab
    const size_t count = m_freeSpace.size();
    for (size_t i = 0; i < count; ++i) {
        FreeSpace::ConstCursor c = m_freeSpace[i];
        const size_t ref = TO_REF(c.ref);

        const size_t ndx = m_slabs.cols().offset.find_pos(ref);
        assert(ndx != size_t(-1));

        const size_t slab_end = TO_REF(m_slabs[ndx].offset);
        const size_t free_end = ref + TO_REF(c.size);

        assert(free_end <= slab_end);
    }
}

void SlabAlloc::Print() const
{
    const size_t allocated = m_slabs.is_empty() ? 0 : (size_t)m_slabs[m_slabs.size()-1].offset;

    size_t free = 0;
    for (size_t i = 0; i < m_freeSpace.size(); ++i) {
        free += TO_REF(m_freeSpace[i].size);
    }

    cout << "Base: " << (m_shared ? m_baseline : 0) << " Allocated: " << (allocated - free) << "\n";
}

#endif //_DEBUG

} //namespace tightdb
