package tn.temporise.domain.model;


import lombok.Builder;

import java.util.Arrays;

@Builder(toBuilder = true)

public record ImageBlogPost(
        Long id,
        String url,
        byte[] image,

        BlogPost blogPost
) {
    @Override
    public String toString() {
        return "ImageBlogPost{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", image=" + Arrays.toString(image) +
                '}';
    }
}