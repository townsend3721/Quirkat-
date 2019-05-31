package qirkat;

import static qirkat.PieceColor.*;
import static qirkat.Command.Type.*;

/** A Player that receives its moves from its Game's getMoveCmnd method.
 *  @author Townsend Saunders
 */
class Manual extends Player {

    /** A Player that will play MYCOLOR on GAME, taking its moves from
     *  GAME. */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
        _prompt = myColor + ": ";
    }

    @Override
    Move myMove() {
        Command command = game().getMoveCmnd(_prompt);
        if (command == null) {
            return null;
        }
        String operands = command.operands()[0];
        int count = 0;
        Move move1 = null;
        while (count + 4 < operands.length()) {
            char col1 = operands.charAt(count);
            char row1 = operands.charAt(count + 1);
            char col2 = operands.charAt(count + 3);
            char row2 = operands.charAt(count + 4);
            move1 = Move.move(move1, Move.move(col1, row1, col2, row2));
            count += 3;
        }
        if (game().board().legalMove(move1)) {
            return move1;
        }
        System.out.println("Illegal move, try again.");
        return myMove();
    }

    /** Identifies the player serving as a source of input commands. */
    private String _prompt;
}

