import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private final List<Document> store = new CopyOnWriteArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        /*
         * For concurrent impl, create new document with new id
         */
        if (document.id == null)
            document.id = IdGenerator.makeId();


        store.add(document);

        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return store.stream()
                .filter(document -> {
                    var titlePrefixesMatch = request.titlePrefixes == null ||
                            request.titlePrefixes.stream().anyMatch(prefix -> document.title.startsWith(prefix));

                    var containsContentsMatch = request.containsContents == null ||
                            request.containsContents.stream().anyMatch(content -> document.content.contains(content));

                    var authorIds = request.authorIds == null ||
                            request.authorIds.stream().anyMatch(authorId -> Objects.equals(authorId, document.author.id));

                    var createdFromMatch = request.createdFrom == null || document.created.isAfter(request.createdFrom);
                    var createdToMatch = request.createdTo == null || document.created.isBefore(request.createdTo);

                    return titlePrefixesMatch && containsContentsMatch && authorIds && createdFromMatch && createdToMatch;
                })
                .toList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return store.stream().filter(document -> Objects.equals(id, document.id)).findFirst();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    @With
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }

    /**
     * Uses crypto stuff to make strong ids
     */
    @UtilityClass
    static class IdGenerator {
        private final static SecureRandom SECURE_RANDOM = new SecureRandom();
        private final static int CHAR_LOWER_BOUND = '!';
        private final static int CHAR_UPPER_BOUND = 'z';

        public static String makeId() {
            var sb = new StringBuilder();
            SECURE_RANDOM.ints(16, CHAR_LOWER_BOUND, CHAR_UPPER_BOUND)
                    .forEach(sb::append);

            return sb.toString();
        }
    }
}