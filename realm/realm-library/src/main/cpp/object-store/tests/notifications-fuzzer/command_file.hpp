#include <realm/link_view_fwd.hpp>

#include <iosfwd>
#include <functional>
#include <memory>
#include <vector>

namespace realm {
    class Table;
    class LinkView;
    class Realm;
    namespace _impl {
        class RealmCoordinator;
    }
}

namespace fuzzer {
struct RealmState {
    realm::Realm& realm;
    realm::_impl::RealmCoordinator& coordinator;

    realm::Table& table;
    realm::LinkViewRef lv;
    int64_t uid;
    std::vector<int64_t> modified;
};

struct CommandFile {
    std::vector<int64_t> initial_values;
    std::vector<size_t> initial_list_indices;
    std::vector<std::function<void (RealmState&)>> commands;

    CommandFile(std::istream& input);

    void import(RealmState& state);
    void run(RealmState& state);
};
}