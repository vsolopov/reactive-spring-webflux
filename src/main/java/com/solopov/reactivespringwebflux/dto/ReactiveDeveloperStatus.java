package com.solopov.reactivespringwebflux.dto;

public record ReactiveDeveloperStatus(
        String firstname,
        String lastname,
        int reactiveProgrammingLevel,
        boolean hasSpringWebfluxExperience) {
}
