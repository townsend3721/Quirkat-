package qirkat;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import static qirkat.Move.*;
import static qirkat.PieceColor.*;

/**
 * A Qirkat board.   The squares are labeled by column (a char value between
 * 'a' and 'e') and row (a char value between '1' and '5'.
 * <p>
 * For some purposes, it is useful to refer to squares using a single
 * integer, which we call its "linearized index".  This is simply the
 * number of the square in row-major order (with row 0 being the bottom row)
 * counting from 0).
 * <p>
 * Moves on this board are denoted by Moves.
 *
 * @author Townsend Saunders
 */
class Board extends Observable {

    /**
     * A new, cleared board at the start of the game.
     */
    Board() {
        clear();
        setChanged();
        notifyObservers();
    }
    /**
     * A copy of B.
     */
    Board(Board b) {
        internalCopy(b);
    }

    /**
     * Return a constant view of me (allows any access method, but no
     * method that modifies it).
     */
    Board constantView() {
        return this.new ConstantBoard();
    }

    /**
     * Clear me to my starting state, with pieces in their initial
     * positions.
     */
    void clear() {
        _whoseMove = WHITE;
        _gameOver = false;
        PieceColor[][] newBoard = {{BLACK, BLACK, BLACK, BLACK, BLACK},
                                   {BLACK, BLACK, BLACK, BLACK, BLACK},
                                   {BLACK, BLACK, EMPTY, WHITE, WHITE},
                                   {WHITE, WHITE, WHITE, WHITE, WHITE},
                                   {WHITE, WHITE, WHITE, WHITE, WHITE}};

        _board = newBoard;
        _lastMoves = new ArrayList<>();

        _leftRight = new ArrayList<>();
        for (int i = 0; i < 5 * 5; i++) {
            ArrayList<String> empty = new ArrayList<>();
            empty.add("e");
            _leftRight.add(empty);
        }
        setChanged();
        notifyObservers();
    }


    /**
     * Copy B into me.
     */
    void copy(Board b) {
        internalCopy(b);
    }

    /**
     * Return row index in standard form, given the linearized index K.
     **/
    int rowI(int k) {
        int x = 5;
        if (k < x) {
            return 4;
        }
        if (k < 2 * x) {
            return 3;
        }
        if (k < 3 * x) {
            return 2;
        }
        if (k < 4 * x) {
            return 1;
        }
        if (k < 5 * x) {
            return 0;
        }
        return -1;
    }

    /**
     * Return my board.
     **/

    public PieceColor[][] getBoard() {
        return _board;
    }

    /**
     * Return column index in standard form, given the linearized index K.
     **/

    int colI(int k) {
        int x = 5;
        if (k < x) {
            return k;
        }
        if (k < 2 * x) {
            return k - x;
        }
        if (k < 3 * x) {
            return k - (2 * x);
        }
        if (k < 4 * x) {
            return k - (3 * x);
        }
        if (k < 5 * x) {
            return k - (4 * x);
        }
        return -1;
    }


    /**
     * Copy B into me.
     */
    private void internalCopy(Board b) {
        _board = b._board;
        _whoseMove = b._whoseMove;
        _gameOver = b._gameOver;
        _lastMoves = b._lastMoves;
        _leftRight = b._leftRight;
    }

    /**
     * Set my contents as defined by STR.  STR consists of 25 characters,
     * each of which is b, w, or -, optionally interspersed with whitespace.
     * These give the contents of the Board in row-major order, starting
     * with the bottom row (row 1) and left column (column a). All squares
     * are initialized to allow horizontal movement in either direction.
     * NEXTMOVE indicates whose move it is.
     */
    void setPieces(String str, PieceColor nextMove) {
        if (nextMove == EMPTY || nextMove == null) {
            throw new IllegalArgumentException("bad player color");
        }
        str = str.replaceAll("\\s", "");
        if (!str.matches("[bw-]{25}")) {
            throw new IllegalArgumentException("bad board description");
        }
        clear();
        _whoseMove = nextMove;

        for (int k = 0; k < str.length(); k += 1) {
            switch (str.charAt(k)) {
            case '-':
                set(k, EMPTY);
                break;
            case 'b':
            case 'B':
                set(k, BLACK);
                break;
            case 'w':
            case 'W':
                set(k, WHITE);
                break;
            default:
                break;
            }
        }

        setChanged();
        notifyObservers();
    }

    /**
     * Return true iff the game is over: i.e., if the current player has
     * no moves.
     */
    boolean gameOver() {
        return _gameOver;
    }

    /**
     * Return the current contents of square C R, where 'a' <= C <= 'e',
     * and '1' <= R <= '5'.
     */
    PieceColor get(char c, char r) {
        assert validSquare(c, r);
        return get(index(c, r));
    }

    /**
     * Return the current contents of the square at linearized index K.
     */
    PieceColor get(int k) {
        assert validSquare(k);
        int row = rowI(k);
        int col = colI(k);
        return _board[row][col];

    }

    /**
     * Set get(C, R) to V, where 'a' <= C <= 'e', and
     * '1' <= R <= '5'.
     */
    private void set(char c, char r, PieceColor v) {
        assert validSquare(c, r);
        set(index(c, r), v);
    }

    /**
     * Set getLR(K) to V, where K is the linearized index of a square.
     */
    private void set(int k, PieceColor v) {
        assert validSquare(k);
        int row = rowI(k);
        int col = colI(k);
        _board[row][col] = v;


    }


    /**
     * Return true iff MOV is legal on the current board.
     */
    boolean legalMove(Move mov) {
        ArrayList<Move> moves = getMoves();
        int to = mov.toIndex();
        int from = mov.fromIndex();
        int size = _leftRight.get(from).size();
        if (mov.isLeftMove()
                && _leftRight.get(from).get(size - 1).equals("L")) {
            return false;
        }
        if (mov.isRightMove()
                && _leftRight.get(mov.fromIndex()).get(size - 1).equals("R")) {
            return false;
        }
        if (validSquare(to) && this.get(to)
                == EMPTY && this.get(from) == _whoseMove) {
            return true;
        }
        return false;
    }

    /**
     * Return a list of all legal moves from the current position.
     */
    ArrayList<Move> getMoves() {
        ArrayList<Move> result = new ArrayList<>();
        getMoves(result);
        return result;
    }

    /**
     * Add all legal moves from the current position to MOVES.
     */
    void getMoves(ArrayList<Move> moves) {
        if (gameOver()) {
            return;
        }
        if (jumpPossible()) {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getJumps(moves, k);
            }
        } else {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getMoves(moves, k);
            }
        }
    }

    /**
     * given a linearized index K return the piece at that position.
     **/
    PieceColor board(int k) {
        int rowN = rowI(k);
        int colN = colI(k);
        return _board[rowN][colN];

    }

    /**
     * Add all legal non-capturing moves from the position
     * with linearized index K to MOVES.
     */
    private void getMoves(ArrayList<Move> moves, int k) {
        moves.add(move(col(k), row(k)));
        if (board(k) == WHITE) {
            if (k > 3 * 5 + 4) {
                return;
            }
            if (validSquare(k + 5)) {
                if (board(k + 5) == EMPTY) {
                    moves.add(move(col(k), row(k), col(k + 5), row(k + 5)));
                }
            }
            if (k % 5 < 4) {
                if (validSquare(k + 1) && board(k + 1) == EMPTY) {
                    moves.add(move(col(k), row(k), col(k + 1), row(k + 1)));
                }
                if (k % 2 == 0) {
                    if (validSquare(k + 6) && board(k + 6) == EMPTY) {
                        moves.add(move(col(k), row(k), col(k + 6), row(k + 6)));
                    }
                }
            }
            if (k % 5 > 0) {
                if (validSquare(k - 1) && board(k - 1) == EMPTY) {
                    moves.add(move(col(k), row(k), col(k - 1), row(k - 1)));
                }
                if (k % 2 == 0) {
                    if (validSquare(k + 4) && board(k + 4) == EMPTY) {
                        moves.add(move(col(k), row(k), col(k + 4), row(k + 4)));
                    }
                }

            }
        } else if (board(k) == BLACK) {
            getmovesBlack(moves, k);
        }
    }
    /** Get moves for black piece given ArrayList MOVES and int K. **/
    private void getmovesBlack(ArrayList<Move> moves, int k) {
        if (k < 5) {
            return;
        }
        if (validSquare(k - 5)) {
            if (board(k - 5) == EMPTY) {
                moves.add(move(col(k), row(k), col(k - 5), row(k - 5)));
            }
        }
        if (k % 5 < 4) {
            if (validSquare(k + 1) && board(k + 1) == EMPTY) {
                moves.add(move(col(k), row(k), col(k + 1), row(k + 1)));
            }
            if (k % 2 == 0) {
                if (validSquare(k - 6) && board(k - 6) == EMPTY) {
                    moves.add(move(col(k), row(k), col(k - 6), row(k - 6)));
                }
            }
        }
        if (k % 5 > 0) {
            if (validSquare(k - 1) && board(k - 1) == EMPTY) {
                moves.add(move(col(k), row(k), col(k - 1), row(k - 1)));
            }
            if (k % 2 == 0) {
                if (validSquare(k - 4) && board(k - 4) == EMPTY) {
                    moves.add(move(col(k), row(k), col(k + 4), row(k + 4)));
                }
            }
        }
    }



    /**
     * Add all legal captures from the position with linearized index K
     * to MOVES.
     */
    private void getJumps(ArrayList<Move> moves, int k) {
        if (!board(k).isPiece()) {
            return;
        }
        if (validSquare(k + 10) && board(k + 5).isPiece()
                && board(k + 5) == board(k).opposite()) {
            if (board(k + 10) == EMPTY) {
                moves.add(move(col(k), row(k), col(k + 10), row(k + 10)));
            }
        }
        if (validSquare(k - 10)
                && board(k - 5).isPiece()
                && board(k - 5) == board(k).opposite()) {
            if (board(k - 10) == EMPTY) {
                moves.add(move(col(k), row(k), col(k - 10), row(k - 10)));
            }
        }
        if (k % 5 < 3) {
            if (validSquare(k + 2) && board(k + 1).isPiece()
                    && board(k + 1) == board(k).opposite()) {
                if (board(k + 2) == EMPTY) {
                    moves.add(move(col(k), row(k), col(k + 2), row(k + 2)));
                }
            }
            if (k % 2 == 0) {
                if (validSquare(k + 12) && board(k + 6).isPiece()
                        && board(k + 6) == board(k).opposite()) {
                    if (board(k + 12) == EMPTY) {
                        moves.add(move(col(k), row(k),
                                col(k + 12), row(k + 12)));
                    }
                }
                if (validSquare(k - 8) && board(k - 4).isPiece()
                        && board(k - 4) == board(k).opposite()) {
                    if (board(k - 8) == EMPTY) {
                        moves.add(move(col(k), row(k), col(k - 8), row(k - 8)));
                    }
                }
            }
        }
        getJumpsHelper(moves, k);
    }

    /** getJumps Helper function that takes in int K and MOVES. **/
    private void getJumpsHelper(ArrayList<Move> moves, int k) {
        if (k % 5 > 1) {
            if (validSquare(k - 2) && board(k - 1).isPiece()
                    && board(k - 1) == board(k).opposite()) {
                if (board(k - 2) == EMPTY) {
                    moves.add(move(col(k), row(k), col(k - 2), row(k - 2)));
                }
            }
            if (k % 2 == 0) {
                if (validSquare(k + 8) && board(k + 4).isPiece()
                        && board(k + 4) == board(k).opposite()) {
                    if (board(k + 8) == EMPTY) {
                        moves.add(move(col(k), row(k), col(k + 8), row(k + 8)));
                    }
                }
                if (validSquare(k - 12) && board(k - 6).isPiece()
                        && board(k - 6) == board(k).opposite()) {
                    if (board(k - 12) == EMPTY) {
                        moves.add(move(col(k), row(k), col(k - 12),
                                row(k - 12)));
                    }
                }
            }
        }
    }


    /**
     * Return true iff MOV is a valid jump sequence on the current board.
     * MOV must be a jump or null.  If ALLOWPARTIAL, allow jumps that
     * could be continued and are valid as far as they go.
     */
    boolean checkJump(Move mov, boolean allowPartial) {
        if (mov == null) {
            return true;
        }
        return legalMove(mov);
    }

    /**
     * Return true iff a jump is possible for a piece at position C R.
     */
    boolean jumpPossible(char c, char r) {
        return jumpPossible(index(c, r));
    }

    /**
     * Return true iff a jump is possible for a piece at position with
     * linearized index K.
     */
    boolean jumpPossible(int k) {
        if (!board(k).isPiece()) {
            return true;
        }
        if (validSquare(k + 10) && board(k + 5).isPiece()
                && board(k + 5) == board(k).opposite()) {
            if (board(k + 10) == EMPTY) {
                return true;
            }
        }
        if (validSquare(k - 10) && board(k - 5).isPiece()
                && board(k - 5) == board(k).opposite()) {
            if (board(k - 10) == EMPTY) {
                return true;
            }
        }

        if (k % 5 < 3) {
            if (validSquare(k + 2) && board(k + 1).isPiece()
                    && board(k + 1) == board(k).opposite()) {
                if (board(k + 2) == EMPTY) {
                    return true;
                }
            }
            if (k % 2 == 0) {
                if (validSquare(k + 12) && board(k + 6).isPiece()
                        && board(k + 6) == board(k).opposite()) {
                    if (board(k + 12) == EMPTY) {
                        return true;
                    }
                }
                if (validSquare(k - 8) && board(k - 4).isPiece()
                        && board(k - 4) == board(k).opposite()) {
                    if (board(k - 8) == EMPTY) {
                        return true;
                    }
                }
            }
        }
        return jumpPossibleHelper(k);
    }
    /** jumpPossible helper function ,returns a boolean given an int K. **/
    boolean jumpPossibleHelper(int k) {
        /** left horizontal moves **/
        if (k % 5 > 1) {
            if (validSquare(k - 2) && board(k - 1).isPiece()
                    && board(k - 1) == board(k).opposite()) {
                if (board(k - 2) == EMPTY) {
                    return true;
                }
            }
            if (k % 2 == 0) {
                if (validSquare(k + 8) && board(k + 4).isPiece()
                        && board(k + 4) == board(k).opposite()) {
                    if (board(k + 8) == EMPTY) {
                        return true;
                    }
                }
                if (validSquare(k - 12) && board(k - 6).isPiece()
                        && board(k - 6) == board(k).opposite()) {
                    if (board(k - 12) == EMPTY) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Return true iff a jump is possible from the current board.
     */
    boolean jumpPossible() {
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            if (jumpPossible(k)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the color of the player who has the next move.  The
     * value is arbitrary if gameOver().
     */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /**
     * Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     * other than pass, assumes that legalMove(C0, R0, C1, R1).
     */
    void makeMove(char c0, char r0, char c1, char r1) {
        makeMove(Move.move(c0, r0, c1, r1, null));
    }

    /**
     * Make the multi-jump C0 R0-C1 R1..., where NEXT is C1R1....
     * Assumes the result is legal.
     */
    void makeMove(char c0, char r0, char c1, char r1, Move next) {
        makeMove(Move.move(c0, r0, c1, r1, next));
    }

    /**
     * Make the Move MOV on this Board, assuming it is legal.
     */
    void makeMove(Move mov) {
        if (legalMove(mov)) {
            if (mov.isJump()) {
                int jumpK = mov.jumpedIndex();
                int toK = mov.toIndex();
                int fromK = mov.fromIndex();
                set(toK, board(fromK));
                set(jumpK, EMPTY);
                set(fromK, EMPTY);
                Move nextMove = mov;
                while (nextMove.jumpTail() != null) {
                    nextMove = nextMove.jumpTail();
                    int nextJumpK = nextMove.jumpedIndex();
                    int nextToK = nextMove.toIndex();
                    int nextFromK = nextMove.fromIndex();
                    set(nextToK, board(nextFromK));
                    set(nextJumpK, EMPTY);
                    set(nextFromK, EMPTY);
                }
                int to = nextMove.toIndex();
                _leftRight.get(to).add("E");

            } else {
                int to = mov.toIndex();
                if (mov.isLeftMove()) {
                    _leftRight.get(to).add("R");
                } else if (mov.isRightMove()) {
                    _leftRight.get(to).add("L");
                } else {
                    _leftRight.get(to).add("E");
                }
                int fromK = mov.fromIndex();
                set(to, board(fromK));
                set(fromK, EMPTY);

            }
            _whoseMove = _whoseMove.opposite();
            _lastMoves.add(mov);
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Go back to previous player's color and RETURN a piece color.
     */
    private PieceColor lastColor() {
        PieceColor back;
        if (whoseMove() == WHITE) {
            _whoseMove = BLACK;
            back = WHITE;
            return back;
        } else {
            _whoseMove = WHITE;
            back = BLACK;
            return back;
        }
    }

    /**
     * Undo the last move, if any.
     */
    void undo() {
        if (!_lastMoves.isEmpty()) {
            Move lastMove = _lastMoves.get(_lastMoves.size() - 1);
            PieceColor color = lastColor();
            if (!lastMove.isJump()) {
                char colOne = lastMove.col0();
                char colTwo = lastMove.col1();
                this.set(index(colOne, lastMove.row0()), whoseMove());
                this.set(index(colTwo, lastMove.row1()), EMPTY);
                int k = index(colTwo, lastMove.row1());
                _leftRight.get(k).remove(_leftRight.get(k).size() - 1);
            } else {
                char colZero = lastMove.col0();
                char colOne = lastMove.col1();
                char jumped = lastMove.jumpedCol();
                char rows = lastMove.row0();
                this.set(index(colZero, lastMove.row0()), whoseMove());
                this.set(index(jumped, lastMove.jumpedRow()), color);
                this.set(index(colOne, lastMove.row1()), EMPTY);
                Move nextMove = lastMove;
                if (nextMove.jumpTail() == null) {
                    int k = index(nextMove.col1(), nextMove.row1());
                    _leftRight.get(k).remove(_leftRight.get(k).size() - 1);
                }
                while (nextMove.jumpTail() != null) {
                    nextMove = nextMove.jumpTail();
                    char drop = nextMove.jumpedCol();
                    this.set(index(drop, nextMove.jumpedRow()), color);
                    if (!(colZero == nextMove.col1()
                            && rows == nextMove.row1())) {
                        this.set(index(nextMove.col1(),
                                nextMove.row1()), EMPTY);
                    }
                    if (nextMove.jumpTail() == null) {
                        int k = index(nextMove.col1(), nextMove.row1());
                        _leftRight.get(k).remove(_leftRight.get(k).size() - 1);
                    }
                }
            }

            _lastMoves.remove(_lastMoves.size() - 1);
            setChanged();
            notifyObservers();
        }
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Return a text depiction of the board.  If LEGEND, supply row and
     * column numbers around the edges.
     */
    String toString(boolean legend) {
        StringBuilder builder = new StringBuilder();
        builder.append("  ");
        for (int i = 0; i < _board.length; i++) {
            for (int j = 0; j < _board[i].length; j++) {
                builder.append(_board[i][j].shortName() + "");
                if (j < _board.length - 1) {
                    builder.append(" ");
                }
            }
            if (i != _board.length - 1) {
                builder.append("\n");
                builder.append("  ");
            }
        }
        return builder.toString();
    }

    /**
     * Return true iff there is a move for the current player.
     */
    private boolean isMove() {
        ArrayList<Move> moves = getMoves();
        if (moves.size() > 0) {
            return true;
        }
        return false;
    }


    /**
     * Player that is on move.
     */
    private PieceColor _whoseMove;
    /**
     * board that contains which position a piece is not allowed to move to.
     */
    private ArrayList<ArrayList<String>> _leftRight;

    /**
     * Set true when game ends.
     */
    private boolean _gameOver;

    /**
     * Game Board.
     **/
    private PieceColor[][] _board;

    /**
     * Last moves.
     **/
    private ArrayList<Move> _lastMoves;

    /**
     * Convenience value giving values of pieces at each ordinal position.
     */
    static final PieceColor[] PIECE_VALUES = PieceColor.values();

    /**
     * One cannot create arrays of ArrayList<Move>, so we introduce
     * a specialized private list type for this purpose.
     */
    private static class MoveList extends ArrayList<Move> {
    }

    /**
     * A read-only view of a Board.
     */
    private class ConstantBoard extends Board implements Observer {
        /**
         * A constant view of this Board.
         */
        ConstantBoard() {
            super(Board.this);
            Board.this.addObserver(this);
        }

        @Override
        void copy(Board b) {
            assert false;
        }

        @Override
        void clear() {
            assert false;
        }

        @Override
        void makeMove(Move move) {
            assert false;
        }

        /**
         * Undo the last move.
         */
        @Override
        void undo() {
            assert false;
        }

        @Override
        public void update(Observable obs, Object arg) {
            super.copy((Board) obs);
            setChanged();
            notifyObservers(arg);
        }
    }
}
