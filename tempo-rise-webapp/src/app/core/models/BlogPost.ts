export interface BlogPost {
  id: number;
  titre: string;
  contenu: string;
  datePublication: string;
  status: 'BROUILLON' | 'PUBLIER' | 'ARCHIVE';
  image?: imageBlogpost[]; // ✅ Ajouté
}
export interface imageBlogpost {
  id?: number;
  url: string;
}