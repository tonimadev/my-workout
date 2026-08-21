# Guia de Arquitetura e Diretrizes do Projeto - My Workout

Este documento serve como referência técnica para desenvolvedores sobre a estrutura, padrões e recomendações do projeto My Workout.

## Visão Geral do Projeto

O My Workout é um aplicativo de rastreamento de treinos desenvolvido com as tecnologias mais modernas do ecossistema Android, focado em **modularização por features**, reusabilidade de código e UI declarativa.

## Estrutura de Módulos

O projeto utiliza uma estrutura **multi-módulos baseada em features** (Padrão Bridge/Impl) para separar responsabilidades e otimizar o tempo de build:

### Core Modules
Contêm lógica e infraestrutura compartilhada:
- **`:core:ui`**: Temas, componentes Material 3 comuns e a base do padrão MVI.
- **`:core:data`**: Centraliza a persistência (Room), modelos de dados, comunicação com wearables e repositórios.
- **`:core:navigation`**: Infraestrutura de navegação (Navigator, chaves base).
- **`:core:common`**: Utilitários e extensões globais.

### Feature Modules (Padrão Bridge/Impl)
Cada funcionalidade de negócio é dividida em dois sub-módulos para maximizar o encapsulamento:
- **`:features:[name]:bridge`**: Define a interface pública da feature, principalmente suas chaves de navegação (`Destination`). Outros módulos dependem apenas do `:bridge`.
- **`:features:[name]:impl`**: Contém a implementação real (Composables, ViewModels, DI). Apenas o módulo `:app` deve depender do `:impl` para realizar a orquestração final.

### App e Wear
- **`:app`**: O orquestrador da versão mobile. Gerencia o grafo de navegação e as dependências `:impl`.
- **`:wear`**: Versão Wear OS, focada na execução rápida de treinos.

## Padrões Arquiteturais

### Bridge/Impl (ou API/Impl)
Este padrão é fundamental para nossa escalabilidade:
1.  **Isolamento**: Mudanças na implementação de uma feature não forçam a recompilação de módulos que apenas a chamam via navegação.
2.  **Controle de Visibilidade**: Apenas o necessário é exposto no módulo `:bridge`.

### MVI (Model-View-Intent)
O projeto segue o padrão MVI para a camada de UI, facilitando a previsibilidade do estado:
- **State**: Um objeto `@Immutable` (ex: `WorkoutState`) que representa tudo o que a tela exibe.
- **Intent**: Uma `sealed interface` (ex: `WorkoutIntent`) representando as ações do usuário.
- **ViewModel**: Processa `Intents` e emite novos `States`.

#### Eventos e Side Effects
Seguimos o padrão de **Eventos como Estado**. Eventos transientes (ex: navegar, mostrar erro) são propriedades do `State`. Após o consumo na UI, o estado deve ser resetado via Intent.

## Recomendações

1.  **Mantenha o Padrão MVI**: Sempre defina explicitamente o `State` e as `Intents`.
2.  **Modularização de Features**: Ao criar uma nova funcionalidade, siga o padrão `:bridge` e `:impl`.
3.  **Core Data**: Toda lógica de persistência ou modelos de entidade devem ser adicionados ao módulo `:core:data`.
4.  **UI Adaptativa**: Utilize componentes do Material 3 Adaptive para suportar diferentes tamanhos de tela.
5.  **Build Performance**: Mantenha as otimizações de Gradle (Parallel, Caching) ativas no `gradle.properties`.
