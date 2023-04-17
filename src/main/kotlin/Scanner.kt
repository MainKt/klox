class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    private val keywords = listOf(
        TokenType.PRINT,
        TokenType.VAR, TokenType.NIL,
        TokenType.TRUE, TokenType.FALSE,
        TokenType.AND, TokenType.OR,
        TokenType.IF, TokenType.ELSE,
        TokenType.FOR, TokenType.WHILE,
        TokenType.FUN, TokenType.RETURN,
        TokenType.CLASS, TokenType.THIS, TokenType.SUPER,
    ).associateBy { it.name.lowercase() }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)

            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)

            '/' -> when {
                match('/') -> while (peek() != '\n' && !isAtEnd()) advance()
                match('*') -> blockComment()
                else -> addToken(TokenType.SLASH)
            }

            // ignore whitespace.
            ' ', '\r', '\t' -> {}

            '\n' -> line++

            '"' -> string()

            else -> when {
                isDigit(c) -> number()
                isAlpha(c) -> identifier()
                else -> error(line, "Unexpected character.")
            }
        }
    }

    private fun blockComment() {
        while (!(peek() == '*' && peekNext() == '/' || isAtEnd())) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            error(line, "Unterminated comment.")
            return
        }

        // Escape the closing */.
        advance()
        advance()
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()

        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (isDigit(peek())) advance()

        if (peek() == '.' && isDigit(peekNext())) {
            advance()

            while (isDigit(peek())) advance()
        }

        addToken(
            TokenType.NUMBER,
            source.substring(start, current).toDouble()
        )
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }

        // The closing ".
        advance()

        // Trim the surrounding quotes.
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun peek() = if (isAtEnd()) 0.toChar() else source[current]

    private fun peekNext() = if (current + 1 >= source.length) 0.toChar() else source[current + 1]

    private fun isDigit(c: Char) = c in '0'..'9'
    private fun isAlpha(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun isAlphaNumeric(c: Char) = isAlpha(c) || isDigit(c)

    private fun isAtEnd() = current >= source.length

    private fun advance() = source[current++]

    private fun addToken(type: TokenType, literal: Any? = null) {
        val lexeme = source.substring(start, current)
        tokens.add(Token(type, lexeme, literal, line))
    }
}
