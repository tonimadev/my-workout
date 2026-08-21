# Guia de Arquitetura e Diretrizes do Projeto - My Workout

Este documento serve como referência técnica para desenvolvedores e agentes de IA sobre a estrutura, padrões e recomendações do projeto My Workout.

## Visão Geral do Projeto

O My Workout é um aplicativo de rastreamento de treinos desenvolvido com as tecnologias mais modernas do ecossistema Android, focado em modularização, reusabilidade de código e UI declarativa.

## Estrutura de Módulos

O projeto utiliza uma estrutura multi-módulos para separar responsabilidades:

- **`:app`**: Módulo principal Android contendo a interface do usuário (UI) e a lógica de navegação.
- **`:shared-data`**: Biblioteca Android que centraliza toda a lógica de persistência (Room), modelos de dados, comunicação com wearables e repositórios. Compartilhada entre `:app` e `:wear`.
- **`:wear`**: Módulo específico para a versão Wear OS do aplicativo.

## Padrões Arquiteturais

### MVI (Model-View-Intent)
O projeto segue o padrão MVI para a camada de UI, facilitando a previsibilidade do estado:
- **State**: Um objeto `@Immutable` (ex: `WorkoutState`) que representa tudo o que a tela exibe, incluindo o controle de eventos transientes.
- **Intent**: Uma `sealed interface` (ex: `WorkoutIntent`) representando as ações do usuário ou eventos do sistema.
- **ViewModel**: Herda de `MviViewModel`, processa `Intents` e emite novos `States`.

#### Eventos e Side Effects
Diferente de algumas implementações de MVI que usam um canal separado para "Side Effects" (como navegação ou toasts), este projeto segue o padrão de **Eventos como Estado**.
- Eventos transientes (ex: "navegar de volta", "mostrar erro") são propriedades do `State` (ex: `shouldNavigateBack: Boolean`).
- Após o consumo do evento na UI, a Intent correspondente deve ser disparada para "resetar" esse estado no ViewModel.

### Clean Architecture
- **Data Layer (`:shared-data`)**: Repositórios (`WorkoutRepository`) encapsulam a lógica de dados, decidindo entre fontes locais e remotas.
- **UI Layer (`:app` / `:wear`)**: Consome os repositórios através de Injeção de Dependência.

## Tecnologias e Bibliotecas Core

- **Linguagem**: Kotlin.
- **UI**: Jetpack Compose com **Material 3**.
- **Navegação**: Custom Navigator com suporte a layouts adaptativos (`NavigationSuiteScaffold`, `ListDetailSceneStrategy`).
- **Injeção de Dependência**: Hilt (Dagger).
- **Async/Stream**: Kotlin Coroutines e Flow.
- **Persistência**: Room (dentro do `:shared-data`).
- **Wearable**: Data Layer API para sincronização entre celular e relógio.

## Recomendações para Agentes de IA

Ao atuar no projeto, siga estas diretrizes:

1.  **Mantenha o Padrão MVI**: Sempre defina explicitamente o `State` e as `Intents` ao criar novas telas. Utilize o `MviViewModel` como base.
2.  **Eventos como Estado**: Siga a prática de incluir eventos transientes (navegação, diálogos, mensagens) dentro do `State` da UI. Lembre-se de implementar a lógica de reset do estado após o consumo do evento no Compose.
3.  **Modularização de Dados**: Toda nova lógica de persistência ou novos modelos de entidade devem ser adicionados ao módulo `:shared-data`. Não adicione lógica de dados pura no módulo `:app`.
3.  **UI Adaptativa**: Utilize componentes do Material 3 Adaptive sempre que possível para garantir que o app funcione bem em diferentes tamanhos de tela (tablets, dobráveis).
4.  **Injeção de Dependência**: Use `@Inject` no construtor e forneça instâncias via Hilt Modules. Evite instanciar classes manualmente.
5.  **Coroutines**: Prefira `viewModelScope` para lançar corrotinas nos ViewModels e `collectAsStateWithLifecycle()` no Compose para consumir fluxos de dados de forma segura.
6.  **Recursos e Strings**: Todas as strings devem ser colocadas em `res/values/strings.xml` (ou variantes de idioma) para suportar internacionalização.
7.  **Documentação**: Mantenha o KDoc atualizado para funções públicas complexas, especialmente em repositórios e lógica de negócio.

---
*Este documento deve ser consultado antes de qualquer refatoração ou adição de novas funcionalidades.*
