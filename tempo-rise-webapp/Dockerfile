FROM node:22.14.0-alpine AS build

WORKDIR /app

COPY . .

RUN npm install --legacy-peer-deps 


RUN npm run build demo --configuration=production 

FROM nginx:1.25.3-alpine

# 1. Remove ALL default configurations and files
RUN rm -rf /etc/nginx/conf.d/* && \
    rm -rf /usr/share/nginx/html/*

# 2. Copy your Angular app (note no trailing slash)
COPY --from=build /app/dist/demo/browser /usr/share/nginx/html

# 3. Copy your custom NGINX full config
COPY nginx.conf /etc/nginx/nginx.conf

# 4. Verify the copy worked
RUN ls -la /usr/share/nginx/html && \
    ls -la /etc/nginx/conf.d/

EXPOSE 80