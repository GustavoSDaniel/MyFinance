FROM quay.io/keycloak/keycloak:23.0

# Copia o seu tema gerado para dentro da imagem
COPY dist_keycloak/keycloak-theme-for-kc-22-to-25.jar /opt/keycloak/providers/

# O comando build otimiza a imagem e já "instala" o tema internamente
RUN /opt/keycloak/bin/kc.sh build