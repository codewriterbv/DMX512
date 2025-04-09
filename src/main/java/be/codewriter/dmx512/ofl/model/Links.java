package be.codewriter.dmx512.ofl.model;

import java.util.List;

public record Links(
        List<String> manual,
        List<String> productPage,
        List<String> video
) {
}
