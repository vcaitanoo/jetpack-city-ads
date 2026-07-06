# Gerar APK pelo celular

Este projeto foi limpo para compilar como Android nativo Kotlin/Jetpack Compose.
Ele não é Unity. É uma versão leve do jogo feita direto em Android.

## Caminho recomendado: GitHub Actions pelo celular

1. No celular, abre github.com e cria um repositório novo chamado `jetpack-city-ads`.
2. Envia todos os arquivos deste projeto para o repositório.
3. Abre a aba **Actions**.
4. Escolhe **Build Android APK**.
5. Toca em **Run workflow**.
6. Quando terminar, abre o workflow e baixa o artifact **JetpackCityAds-debug-apk**.
7. Dentro dele estará o APK: `app-debug.apk`.

## Observação

O APK debug serve para testar no telemóvel. Para publicar na Play Store depois, gere AAB assinado.
