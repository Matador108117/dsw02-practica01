FROM node:22-alpine AS build
WORKDIR /app

COPY package*.json ./
# Cypress is a dev dependency used for tests; skip binary download in image builds.
ENV CYPRESS_INSTALL_BINARY=0
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist/empleados-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD nginx -t || exit 1
