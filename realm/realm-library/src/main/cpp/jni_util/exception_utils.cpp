#include <unwind.h>
#include <dlfcn.h>
#include <iomanip>
#include <android/log.h>

struct BacktraceState
{
    void** current;
    void** end;
};

static _Unwind_Reason_Code unwindCallback(struct _Unwind_Context* context, void* arg)
{
    BacktraceState* state = static_cast<BacktraceState*>(arg);
    uintptr_t pc = _Unwind_GetIP(context);
    if (pc) {
        if (state->current == state->end) {
            return _URC_END_OF_STACK;
        } else {
            *state->current++ = reinterpret_cast<void*>(pc);
        }
    }
    return _URC_NO_REASON;
}

size_t captureBacktrace(void** buffer, size_t max)
{
    BacktraceState state = {buffer, buffer + max};
    _Unwind_Backtrace(unwindCallback, &state);

    return state.current - buffer;
}

void dumpBacktrace(std::ostream& os, void** buffer, size_t count)
{
    for (size_t idx = 0; idx < count; ++idx) {
        uintptr_t addr = reinterpret_cast<uintptr_t>(buffer[idx]);
        uintptr_t base_addr = 0;

        Dl_info info;
        if (dladdr(reinterpret_cast<void*>(addr), &info)) {
            base_addr = reinterpret_cast<uintptr_t>(info.dli_fbase);
        }

        void* real_addr = (void *) (addr - base_addr);

        os << " " << real_addr ;
    }
}

extern "C" {
void __real___cxa_throw(void *ex, void *info, void (*dest)(void *));

void __wrap___cxa_throw(void *ex, void *info, void (*dest)(void *))
{
    const size_t max = 30;
    void* buffer[max];
    std::ostringstream oss;

    dumpBacktrace(oss, buffer, captureBacktrace(buffer, max));

    __android_log_print(ANDROID_LOG_ERROR, "Backtrace for Native exception: ", "%s", oss.str().c_str());
    __real___cxa_throw(ex,info,dest);
}
}
