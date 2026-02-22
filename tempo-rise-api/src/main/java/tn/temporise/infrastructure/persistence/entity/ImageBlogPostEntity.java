package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@Entity
@Table(name = "image_blogpost")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageBlogPostEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String url;
  private byte[] image; // Champ ajouté pour l'image binaire
  @ManyToOne
  @JoinColumn(name = "blogpost_id", nullable = false)
  private BlogPostEntity blogPost;

  @Override
  public String toString() {
    return "ImageBlogPostEntity{" + "id=" + id + ", url='" + url + '\'' + ", image="
        + Arrays.toString(image) + '}';
  }
}
