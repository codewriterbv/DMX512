package be.codewriter.dmx512.ofl.model;

import java.util.List;

public record Meta(
        List<String> authors,
        String createDate,
        String lastModifyDate
) {
}