package be.codewriter.dmx512.ofl.model;

import java.util.List;

/**
 * Meta info about a fixture definition
 *
 * @param authors        list of authors
 * @param createDate     creation date
 * @param lastModifyDate last modified date
 */
public record Meta(
        List<String> authors,
        String createDate,
        String lastModifyDate
) {
}