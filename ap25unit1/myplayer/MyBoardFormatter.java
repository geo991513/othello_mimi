package myplayer;

import static ap25.Board.*;
import static ap25.Color.*;

import java.util.List;
import java.util.Map;

import ap25.*;

/**
 * オセロの盤面を文字列として整形するクラス
 * 盤面の状態、合法手、最後の手を表示
 */
public class MyBoardFormatter {
  /**
   * 盤面を文字列として整形するメソッド
   * @param board 整形対象の盤面
   * @return 整形された盤面の文字列
   */
  public static String format(MyBoard board) {
    var turn = board.getTurn();  // 現在の手番
    var move = board.getMove();  // 最後の手
    var blacks = board.findNoPassLegalIndexes(BLACK);  // 黒の合法手
    var whites = board.findNoPassLegalIndexes(WHITE);  // 白の合法手
    var legals = Map.of(BLACK, blacks, WHITE, whites);  // 色ごとの合法手をマップ化

    // 列番号（a-h）を表示
    var buf = new StringBuilder("  ");
    for (int k = 0; k < SIZE; k++) buf.append(Move.toColString(k));
    buf.append("\n");

    // 盤面の各マスを表示
    for (int k = 0; k < SIZE * SIZE; k++) {
      int col = k % SIZE;  // 列（0-7）
      int row = k / SIZE;  // 行（0-7）

      // 行番号（1-8）を表示
      if (col == 0) buf.append((row + 1) + "|");

      // マスの状態を表示
      if (board.get(k) == NONE) {
        // 空きマスの場合
        boolean legal = false;
        var b = blacks.contains(k);  // 黒の合法手か
        var w = whites.contains(k);  // 白の合法手か
        if (turn == BLACK && b) legal = true;  // 黒の手番で合法手なら
        if (turn == WHITE && w) legal = true;  // 白の手番で合法手なら
        buf.append(legal ? '.' : ' ');  // 合法手なら'.'、それ以外は空白
      } else {
        // 石が置かれている場合
        var s = board.get(k).toString();
        if (move != null && k == move.getIndex()) s = s.toUpperCase();  // 最後の手なら大文字で表示
        buf.append(s);
      }

      // 行の終わりで追加情報を表示
      if (col == SIZE - 1) {
        buf.append("| ");
        if (row == 0 && move != null) {
          buf.append(move);  // 1行目に最後の手を表示
        } else if (row == 1) {
          buf.append(turn + ": " + toString(legals.get(turn)));  // 2行目に現在の手番と合法手を表示
        }
        buf.append("\n");
      }
    }

    buf.setLength(buf.length() - 1);  // 最後の改行を削除
    return buf.toString();
  }

  /**
   * 合法手のリストを文字列のリストに変換
   * @param moves 合法手のインデックスリスト
   * @return 合法手の文字列表現のリスト
   */
  static List<String> toString(List<Integer> moves) {
    return moves.stream().map(k -> Move.toIndexString(k)).toList();
  }
}
