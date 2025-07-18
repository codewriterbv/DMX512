package be.codewriter.dmx512.ofl.model;

import java.util.List;

/**
 * Set of links
 *
 * @param manual      link to the manual
 * @param productPage link to the product page
 * @param video       link to the video
 */
public record Links(
        List<String> manual,
        List<String> productPage,
        List<String> video
) {
}
