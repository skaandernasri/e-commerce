INSERT INTO section (titre, type, is_active, type_page, created_at, updated_at, created_by, updated_by, image_url, contenu_json) VALUES
('product', 'OUR_PRODUCTS', true, 'PRODUCT',
 '2025-11-20 08:13:44.606615', '2025-11-20 12:57:39.024932',
 '1', '1', NULL,
 '{"title":"Our products"}'),

('blog', 'OUR_BLOGS', true, 'BLOG',
 '2025-11-20 09:14:02.612443', '2025-11-20 12:51:30.472951',
 '1', '1', NULL,
 '{"title":"Our Blogs"}'),

('main', 'MAIN', true, 'PRODUCT_DETAILS',
 '2025-11-20 12:32:56.239821', '2025-11-20 12:49:23.470623',
 '1', '1', NULL,
 NULL),

('review', 'OUR_REVIEWS', true, 'PRODUCT_DETAILS',
 '2025-11-20 12:38:28.864156', '2025-11-20 12:49:39.231606',
 '1', '1', NULL,
 NULL),

('description', 'DESCRIPTION', true, 'PRODUCT_DETAILS',
 '2025-11-20 12:56:07.934059', NULL,
 '1', NULL, NULL,
 NULL)