package sd.p02.day14;

import java.util.List;

/**
 * TODO(day14): the maintenance role - used only by the nightly job. Exactly five methods:
 *
 * <pre>
 *   void bulkImport(List&lt;User&gt; users);
 *   void reindex();
 *   void purgeDeleted();
 *   void rebuildStatistics();
 *   void vacuum();
 * </pre>
 */
public interface UserAdmin {
    void bulkImport(List<User> users);
    void reindex();
    void purgeDeleted();
    void rebuildStatistics();
    void vacuum();
}
