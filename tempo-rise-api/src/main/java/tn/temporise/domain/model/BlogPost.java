package tn.temporise.domain.model;



import lombok.Builder;


import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)

public record BlogPost(

        Long id,
        String titre,
        String contenu,
        LocalDateTime date_publication,

        UtilisateurModel user,
        BlogPostStatus status,
        List<ImageBlogPost> image

) {

    @Override
    public String toString() {
        return "BlogPost{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", date_publication=" + date_publication +
                ", status=" + status +
                ", image=" + image +
                '}';
    }

    public BlogPost withUser(UtilisateurModel user) {
        return new BlogPost(id, titre, contenu, date_publication, user,status,image);
    }
    public BlogPost withId(Long id) {
        return new BlogPost(id, titre, contenu, date_publication, user,status,image);
    }





    public void setId(Long id) {
    }

}