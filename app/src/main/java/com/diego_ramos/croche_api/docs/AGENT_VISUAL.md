# Tarefa: Reformulação visual do aplicativo Crochê da Nanni

## Objetivo

Atualize a identidade visual do aplicativo mobile de gerenciamento de vendas e pedidos de crochê, adotando como referência visual o ícone oficial do negócio, disponível na imagem de referência fornecida.

A nova interface deve transmitir uma identidade artesanal, delicada, acolhedora e profissional, mantendo a usabilidade de um aplicativo administrativo.

## 1. Antes de implementar

- Analise a estrutura atual do projeto e identifique como o tema visual, as cores e os componentes de interface estão organizados.
- Identifique todas as telas já implementadas e os componentes compartilhados.
- Verifique a versão do Material Design 3 e a configuração atual do Jetpack Compose.
- Apresente brevemente o plano de alteração antes de começar a modificar os arquivos.

Não recrie o projeto do zero. Trabalhe sobre a implementação existente.

## 2. Paleta de cores

Adote a seguinte paleta como base da identidade visual:

- **Fundo principal:** `#F8F3EF` — creme suave.
- **Cor primária:** `#B77A43` — caramelo.
- **Cor primária escura:** `#75472F` — marrom café.
- **Superfícies secundárias:** `#E9D3BF` — bege rosado.
- **Destaques decorativos:** `#D9A0A0` — rosa delicado.
- **Superfícies de cards e diálogos:** `#FFFFFF`.
- **Texto principal:** marrom muito escuro ou preto, garantindo contraste adequado.
- **Texto secundário:** marrom acinzentado, mantendo legibilidade.

Esses códigos são referências iniciais e podem ser ajustados ligeiramente para assegurar contraste e acessibilidade.

## 3. Aplicação da identidade visual

Aplique a paleta de maneira consistente em todas as telas existentes:

- **Barra superior:** fundo claro, títulos em marrom escuro e ícones harmonizados com a identidade.
- **Botões primários:** caramelo com texto de alto contraste, estados de pressionamento e indicação de desabilitado.
- **Botões secundários:** estilo discreto, usando tons de bege e marrom.
- **Cards:** superfícies brancas ou creme, com bordas sutis e separação visual adequada.
- **Campos de formulário:** fundos claros, bordas discretas e estados de foco e erro claramente identificáveis.
- **Navegação:** itens selecionados em caramelo ou marrom, com estados não selecionados mais discretos.
- **Listas de pedidos e produtos:** hierarquia visual clara, com informações importantes fáceis de localizar.
- **Valores monetários e totais:** destaque tipográfico sem excesso de elementos decorativos.
- **Diálogos, menus e mensagens:** seguir a mesma identidade visual.
- **Estados vazios:** mensagens acolhedoras e discretas, sem comprometer a clareza administrativa.

Utilize rosa e bege como cores complementares, sem competir com a cor primária.

## 4. Tema e organização do código

- Centralize a paleta e a configuração do tema nos arquivos de tema já existentes, utilizando os recursos do Material Design 3 quando apropriado.
- Utilize `ColorScheme` e os componentes temáticos do Compose para evitar cores espalhadas como valores literais pelas telas.
- Respeite a organização atual dos pacotes e a arquitetura feature-based do projeto.
- Prefira componentes reutilizáveis e tema compartilhado quando isso reduzir duplicação real.
- Preserve o suporte aos estados de interação e aos componentes nativos do Material Design 3.

## 5. Direção visual

A interface deve parecer um aplicativo de um pequeno negócio artesanal, não um painel corporativo genérico.

Priorize:
- Aparência limpa, leve e acolhedora.
- Cantos moderadamente arredondados.
- Espaçamento consistente.
- Tipografia legível e hierarquia clara.
- Uso moderado de bordas, sombras e cores decorativas.
- Consistência entre todas as telas.
- Facilidade de uso no dia a dia, inclusive para uma pessoa que não tenha familiaridade com aplicativos complexos.

Evite excesso de rosa, fundos muito saturados, sombras fortes, gradientes desnecessários e elementos decorativos que prejudiquem a leitura.

## 6. Restrições importantes

Esta tarefa é exclusivamente uma reformulação visual.

- Não altere as regras de negócio.
- Não altere entidades, contratos de repositório, serviços ou a persistência Room/SQLite.
- Não modifique operações de cadastro, edição, ativação, desativação, consulta ou gerenciamento de pedidos e produtos.
- Não remova funcionalidades existentes.
- Não adicione dependências sem necessidade comprovada.
- Não implemente novas telas ou funcionalidades como parte desta tarefa.
- Não substitua componentes ou estruturas funcionais sem justificativa técnica.

Aplique as alterações visuais sem comprometer o comportamento atual do aplicativo.

## 7. Validação e conclusão

Após implementar:

1. Compile o projeto e corrija eventuais erros introduzidos pelas alterações.
2. Verifique se todas as telas existentes utilizam a nova identidade visual.
3. Confira os estados de foco, erro, carregamento, desabilitado e seleção, quando aplicáveis.
4. Verifique se os textos e botões continuam legíveis e acessíveis.
5. Informe quais arquivos foram modificados e o motivo das alterações.
6. Informe o resultado da compilação e quaisquer verificações que não tenham sido executadas.

Não declare testes executados se eles não tiverem sido realizados.

**Critério de conclusão:** o aplicativo deve manter suas funcionalidades atuais, apresentar uma identidade visual coerente com o ícone do Crochê da Nanni e possuir uma configuração de tema centralizada, fácil de manter e ampliar.