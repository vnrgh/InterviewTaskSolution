import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    private final Map<String, Document> storage = new HashMap<>();
    private int counter = 1;

    public Document save(Document document) {
        if (document.getId() == null) {
            while (storage.containsKey(String.valueOf(counter))) {
                counter++;
            }
            String newId = String.valueOf(counter++);
            document.setId(newId);
            if (document.getCreated() == null) {
                document.setCreated(Instant.now());
            }
            storage.put(document.getId(), document);
        } else {
            storage.merge(document.getId(), document, (existingDocument, newDocument) -> {
                newDocument.setCreated(existingDocument);
                return newDocument;
            });
        }
        return storage.get(document.getId());
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(document -> filterByTitlePrefixes(document, request.getTitlePrefixes()))
                .filter(document -> filterByContainsContents(document, request.getContainsContents()))
                .filter(document -> filterByAuthorIds(document, request.getAuthorIds()))
                .filter(document -> filterByCreatedFrom(document, request.getCreatedFrom()))
                .filter(document -> filterByCreatedTo(document, request.getCreatedTo()))
                .toList();
    }

    private boolean checkForNullAndIsEmpty(List<String> provided) {
        return provided == null || provided.isEmpty();
    }

    private boolean filterByTitlePrefixes(Document document, List<String> titlePrefixes) {
        if (checkForNullAndIsEmpty(titlePrefixes)) return true;
        return titlePrefixes.stream()
                .anyMatch(prefix -> document.getTitle() != null && document.getTitle().startsWith(prefix));
    }

    private boolean filterByContainsContents(Document document, List<String> containsContents) {
        if (checkForNullAndIsEmpty(containsContents)) return true;
        return containsContents.stream()
                .anyMatch(content -> document.getContent() != null && document.getContent().contains(content));
    }

    private boolean filterByAuthorIds(Document document, List<String> authorIds) {
        if (checkForNullAndIsEmpty(authorIds)) return true;
        return document.getAuthor() != null && authorIds.contains(document.getAuthor().getId());
    }

    private boolean filterByCreatedFrom(Document document, Instant createdFrom) {
        if (createdFrom == null) {
            return true;
        }
        Instant created = document.getCreated();
        if (created == null) {
            return false;
        }
        return !created.isBefore(createdFrom);
    }

    private boolean filterByCreatedTo(Document document, Instant createdTo) {
        if (createdTo == null) {
            return true;
        }
        Instant created = document.getCreated();
        if (created == null) {
            return false;
        }
        return !created.isAfter(createdTo);
    }


    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
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
}