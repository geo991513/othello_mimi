package myplayer;

import java.util.List;

import ap25.*;

/**
 * 人間プレイヤーの実装。
 * ユーザーからの入力を受け取り、合法手の中から選択する。
 */
public class HumanPlayer extends ap25.Player {
    /**
     * プレイヤー名
     */
    static final String MY_NAME = "HUMAN";

    /**
     * コンストラクタ。プレイヤー名と色を設定する。
     */
    public HumanPlayer(Color color) {
        super(MY_NAME, color);
    }

    /**
     * ユーザーからの入力を待ち、入力された手を返す。
     */
    public Move think(Board board) {
        List<Move> legalMoves = board.findLegalMoves(getColor());
        System.out.println("有効な手：" + legalMoves);
        System.out.print("次の手を入力してください： ");
        String input = null;
        Move move = null;

        while (move == null) {
            input = System.console().readLine().trim();
            if (input.isEmpty()) {
                System.out.println("入力が空です。もう一度入力してください。");
                continue;
            } else {
                try {
                    move = Move.of(input, getColor());
                    if (!legalMoves.contains(move)) {
                        System.out.println("不正な手です。有効な手を入力してください。");
                        move = null;
                    }
                } catch (Exception e) {
                    System.out.println("不正な入力です。正しい形式で入力してください。");
                }
            }
        }
        return move;
    }
}
