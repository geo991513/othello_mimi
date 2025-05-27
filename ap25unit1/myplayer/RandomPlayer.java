package myplayer;

import ap25.*;
import java.util.Random;

/**
 * ランダムな手を選択するシンプルなオセロプレイヤー実装。
 * 合法手の中からランダムに一手を選ぶ戦略を持つ。
 * テストやベースラインプレイヤーとして使用される。
 */
public class RandomPlayer extends Player {
  /**
   * 乱数生成器。手の選択に使用される。
   */
  Random rand = new Random();

  /**
   * 指定された色のランダムプレイヤーを作成するコンストラクタ。
   * プレイヤー名は"R"に固定される。
   */
  public RandomPlayer(Color color) {
    super("R", color);
  }

  /**
   * プレイヤーの思考ルーチン。
   * 可能な合法手のリストからランダムに一手を選択して返す。
   */
  public Move think(Board board) {
    var moves = board.findLegalMoves(getColor());
    var i = this.rand.nextInt(moves.size());
    return moves.get(i);
  }
}
