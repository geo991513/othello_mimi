package ap25;

import java.util.List;

/**
 * オセロの盤面を表すインターフェース
 * 盤面の状態管理と操作のための基本的な機能を定義
 */
public interface Board {
  /** 盤面のサイズ（縦横のマス数） */
  int SIZE = 6;
  /** 盤面の総マス数 */
  int LENGTH = SIZE * SIZE;

  /**
   * 指定された位置の石の色を取得
   * @param k 盤面の位置（0からLENGTH-1）
   * @return 石の色（BLACK, WHITE, NONE）
   */
  Color get(int k);

  /**
   * 最後に打たれた手を取得
   * @return 最後の手（最初の手の場合はnull）
   */
  Move getMove();

  /**
   * 現在の手番を取得
   * @return 手番の色（BLACK, WHITE）
   */
  Color getTurn();

  /**
   * 指定された色の石の数を数える
   * @param color 数える石の色
   * @return 石の数
   */
  int count(Color color);

  /**
   * ゲームが終了しているかどうかを判定
   * @return 終了していればtrue
   */
  boolean isEnd();

  /**
   * 勝者を取得
   * @return 勝者の色（引き分けの場合はNONE）
   */
  Color winner();

  /**
   * 反則を記録
   * @param color 反則をしたプレイヤーの色
   */
  void foul(Color color);

  /**
   * スコアを取得
   * @return 黒の石の数 - 白の石の数
   */
  int score();

  /**
   * 指定された色の合法手を全て取得
   * @param color 手番の色
   * @return 合法手のリスト
   */
  List<Move> findLegalMoves(Color color);

  /**
   * 指定された手を打った後の盤面を取得
   * @param move 打つ手
   * @return 新しい盤面
   */
  Board placed(Move move);

  /**
   * 盤面を反転させた盤面を取得
   * @return 反転した盤面
   */
  Board flipped();

  /**
   * 盤面の複製を取得
   * @return 複製された盤面
   */
  Board clone();
}
