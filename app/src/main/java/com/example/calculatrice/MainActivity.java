package com.example.calculatrice;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tvResult;
    private String currentInput = "";
    private boolean isNewExpression = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResult = findViewById(R.id.tvResult);
        tvResult.setText("0");

        // Numéros
        setupNumberButton(R.id.btn0, "0");
        setupNumberButton(R.id.btn1, "1");
        setupNumberButton(R.id.btn2, "2");
        setupNumberButton(R.id.btn3, "3");
        setupNumberButton(R.id.btn4, "4");
        setupNumberButton(R.id.btn5, "5");
        setupNumberButton(R.id.btn6, "6");
        setupNumberButton(R.id.btn7, "7");
        setupNumberButton(R.id.btn8, "8");
        setupNumberButton(R.id.btn9, "9");

        // Point
        findViewById(R.id.btnDot).setOnClickListener(v -> handleDot());

        // Opérateurs
        setupOperatorButton(R.id.btnPlus, "+");
        setupOperatorButton(R.id.btnMinus, "-");
        setupOperatorButton(R.id.btnMultiply, "*");
        setupOperatorButton(R.id.btnDivide, "/");

        // Parenthèses et autres opérateurs
        findViewById(R.id.btnOpen).setOnClickListener(v -> handleOpenParenthesis());
        findViewById(R.id.btnClose).setOnClickListener(v -> handleCloseParenthesis());
        findViewById(R.id.btnPower).setOnClickListener(v -> handleOperator("^"));
        findViewById(R.id.btnPercent).setOnClickListener(v -> handlePercent());

        // Clear
        findViewById(R.id.btnClear).setOnClickListener(v -> clearCalculator());

        // "="
        findViewById(R.id.btnEquals).setOnClickListener(v -> calculate());
    }

    private void setupNumberButton(int id, String value) {
        Button btn = findViewById(id);
        btn.setOnClickListener(v -> {
            if (isNewExpression) {
                currentInput = "";
                isNewExpression = false;
            }

            currentInput += value;
            tvResult.setText(currentInput);
        });
    }

    private void handleDot() {
        if (isNewExpression) {
            currentInput = "0.";
            isNewExpression = false;
        } else if (currentInput.isEmpty()) {
            currentInput = "0.";
        } else {
            // Vérifier si le dernier nombre contient déjà un point
            String[] parts = currentInput.split("[+\\-*/()^]");
            String lastPart = parts[parts.length - 1];
            if (!lastPart.contains(".")) {
                currentInput += ".";
            }
        }
        tvResult.setText(currentInput);
    }

    private void setupOperatorButton(int id, String op) {
        Button btn = findViewById(id);
        btn.setOnClickListener(v -> handleOperator(op));
    }

    private void handleOperator(String op) {
        if (currentInput.isEmpty()) {
            // Si vide, on peut commencer par - pour les nombres négatifs
            if (op.equals("-")) {
                currentInput = "-";
                tvResult.setText(currentInput);
                isNewExpression = false;
            }
            return;
        }

        char lastChar = currentInput.charAt(currentInput.length() - 1);

        // Remplacer l'opérateur précédent si c'en était un
        if (isOperator(String.valueOf(lastChar))) {
            currentInput = currentInput.substring(0, currentInput.length() - 1) + op;
        } else if (lastChar == '(' && !op.equals("-")) {
            // Après '(', seulement - est autorisé pour les nombres négatifs
            return;
        } else {
            currentInput += op;
        }

        tvResult.setText(currentInput);
        isNewExpression = false;
    }

    private void handleOpenParenthesis() {
        if (isNewExpression || currentInput.isEmpty()) {
            currentInput = "(";
        } else {
            char lastChar = currentInput.charAt(currentInput.length() - 1);
            if (isOperator(String.valueOf(lastChar)) || lastChar == '(') {
                currentInput += "(";
            }
        }
        tvResult.setText(currentInput);
        isNewExpression = false;
    }

    private void handleCloseParenthesis() {
        if (currentInput.isEmpty()) return;

        int openCount = countOccurrences(currentInput, '(');
        int closeCount = countOccurrences(currentInput, ')');

        if (openCount > closeCount) {
            char lastChar = currentInput.charAt(currentInput.length() - 1);
            if (Character.isDigit(lastChar) || lastChar == ')') {
                currentInput += ")";
            }
        }
        tvResult.setText(currentInput);
        isNewExpression = false;
    }

    private void handlePercent() {
        if (currentInput.isEmpty()) return;

        try {
            // Évaluer l'expression et calculer le pourcentage
            double result = evaluateExpression(currentInput);
            result = result / 100.0;

            currentInput = formatResult(result);
            tvResult.setText(currentInput);
            isNewExpression = true;

        } catch (Exception e) {
            tvResult.setText("Error");
            currentInput = "";
            isNewExpression = true;
        }
    }

    private void calculate() {
        if (currentInput.isEmpty()) {
            return;
        }

        try {
            double result = evaluateExpression(currentInput);
            currentInput = formatResult(result);
            tvResult.setText(currentInput);
            isNewExpression = true;

        } catch (Exception e) {
            tvResult.setText("Error");
            currentInput = "";
            isNewExpression = true;
        }
    }

    private double evaluateExpression(String expression) {
        try {
            // Utiliser notre propre évaluateur
            return eval(expression);
        } catch (Exception e) {
            throw new RuntimeException("Invalid expression: " + e.getMessage());
        }
    }

    // Évaluateur d'expressions mathématiques avec support des parenthèses
    private double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected character: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) {
                        double divisor = parseFactor();
                        if (divisor == 0) throw new RuntimeException("Division by zero");
                        x /= divisor;
                    }
                    else if (eat('^')) x = Math.pow(x, parseFactor());
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                return x;
            }
        }.parse();
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.valueOf((long) result);
        } else {
            // Limiter à 6 décimales maximum
            String formatted = String.valueOf(result);
            if (formatted.contains(".") && formatted.length() > 8) {
                formatted = String.format("%.6f", result).replaceAll("0*$", "").replaceAll("\\.$", "");
            }
            return formatted;
        }
    }

    private boolean hasUnmatchedParentheses() {
        int openCount = countOccurrences(currentInput, '(');
        int closeCount = countOccurrences(currentInput, ')');
        return openCount != closeCount;
    }

    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }

    private boolean isOperator(String str) {
        return str.equals("+") || str.equals("-") || str.equals("*") || str.equals("/") ||
                str.equals("^");
    }

    private void clearCalculator() {
        tvResult.setText("0");
        currentInput = "";
        isNewExpression = true;
    }
}