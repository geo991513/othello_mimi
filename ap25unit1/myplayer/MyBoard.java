package myplayer;

import static ap25.Color.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ap25.*;

/**
 * オセロのボード状態を管理するクラス
 * ボードの状態、石の配置、合法手の判定などの機能を提供する
 */
public class MyBoard implements Board, Cloneable {
  /** オセロボードの状態を表す配列 */
  long board_white;
  long board_black;
  long board_obstacle;
  /** 最後に打たれた手 */
  Move move = Move.ofPass(NONE);

  /**
   * デフォルトコンストラクタ
   * 空のボードを作成し、初期配置を設定する
   */
  public MyBoard() {
    this.board_black = 0L;
    this.board_white = 0L;
    this.board_obstacle = 0L;
    init();
  }

  /**
   * 既存のボード状態をコピーして新しいボードを作成する
   * 
   * @param board 既存のボード状態
   * @param move  最後に打たれた手
   */
  MyBoard(long board_black, long board_white, long board_obstacle, Move move) {
    this.board_black = board_black;
    this.board_white = board_white;
    this.board_obstacle = board_obstacle;
    this.move = move;
  }

  // myboardのコピーを作成する
  public MyBoard clone() {
    return new MyBoard(this.board_black, this.board_white, this.board_obstacle, this.move);
  }

  // 初期配置
  void init() {
    set(Move.parseIndex("c3"), BLACK);
    set(Move.parseIndex("d4"), BLACK);
    set(Move.parseIndex("d3"), WHITE);
    set(Move.parseIndex("c4"), WHITE);
  }

  public Color get(int k) {
    if ((this.board_obstacle & (1L << k)) != 0) {
      return BLOCK;
    } else if ((this.board_black & (1L << k)) != 0) {
      return BLACK;
    } else if ((this.board_white & (1L << k)) != 0) {
      return WHITE;
    }
    return NONE;
  }

  public Move getMove() {
    return this.move;
  }

  /**
   * 現在の手番の色を取得する
   * 
   * @return 手番の色（BLACK/WHITE）
   */
  public Color getTurn() {
    return this.move.isNone() ? BLACK : this.move.getColor().flipped();
  }

  /**
   * 指定した位置に色をセットする
   * 
   * @param k     セットする位置のインデックス
   * @param color セットする色
   */
  public void set(int k, Color color) {
    if (color == BLOCK) {
      this.board_obstacle |= (1L << k);
    } else if (color == BLACK) {
      this.board_black |= (1L << k);
      this.board_white &= ~(1L << k);
      this.board_obstacle &= ~(1L << k);
    } else if (color == WHITE) {
      this.board_white |= (1L << k);
      this.board_black &= ~(1L << k);
      this.board_obstacle &= ~(1L << k);
    } else {
      this.board_black &= ~(1L << k);
      this.board_white &= ~(1L << k);
      this.board_obstacle &= ~(1L << k);
    }
  }

  /**
   * ボードの状態を比較する
   * 
   * @param otherObj 比較対象のオブジェクト
   * @return 両者のボード状態が同じ場合true
   */
  public boolean equals(Object otherObj) {
    if (otherObj instanceof MyBoard) {
      var other = (MyBoard) otherObj;
      return this.board_black == other.board_black &&
          this.board_white == other.board_white &&
          this.board_obstacle == other.board_obstacle;
    }
    return false;
  }

  /**
   * ボードの状態を文字列として返す
   * MyBoardFormatterを使用して整形された文字列を生成する
   * 
   * @return フォーマットされたボード状態の文字列表現
   */
  public String toString() {
    return MyBoardFormatter.format(this);
  }

  /**
   * 指定された色の石の数を数える
   * 
   * @param color カウントする色
   * @return その色の石の数
   */
  public int count(Color color) {
    return countAll().getOrDefault(color, 0L).intValue();
  }

  /**
   * ゲームが終了したかどうかを判定する
   * 
   * @return 黒白両方とも合法手がない場合true
   */
  public boolean isEnd() {
    var lbs = findNoPassLegalIndexes(BLACK);
    var lws = findNoPassLegalIndexes(WHITE);
    return lbs.size() == 0 && lws.size() == 0;
  }

  /**
   * 勝者の色を返す
   * 
   * @return 勝者の色（BLACK/WHITE）。引き分けまたはゲーム継続中の場合はNONE
   */
  public Color winner() {
    var v = score();
    if (isEnd() == false || v == 0)
      return NONE;
    return v > 0 ? BLACK : WHITE;
  }

  /**
   * 反則負けの処理を行う
   * 
   * @param color 反則を行ったプレイヤーの色
   */
  public void foul(Color color) {
    if (color == BLACK) {
      this.board_black = 0L;
      this.board_white = Long.MAX_VALUE;
    } else if (color == WHITE) {
      this.board_black = Long.MAX_VALUE;
      this.board_white = 0L;
    }
  }

  /**
   * 現在の盤面のスコアを計算する
   * 黒の石数から白の石数を引いた値を返す
   * 片方の色の石が0個の場合、空きマスも勝者の石としてカウントする
   * 
   * @return スコア（正：黒有利、負：白有利）
   */
  public int score() {
    var cs = countAll(); // 各色の石の数をカウント
    var bs = cs.getOrDefault(BLACK, 0L);
    var ws = cs.getOrDefault(WHITE, 0L);
    var ns = LENGTH - bs - ws; // 空きマスの数
    int score = (int) (bs - ws);

    if (bs == 0 || ws == 0)
      score += Integer.signum(score) * ns;

    return score;
  }

  /**
   * ボード上の各色の石の数をカウントする
   * 
   * @return 色をキー、その色の石の数を値とするマップ
   */
  Map<Color, Long> countAll() {
    int blackCount = Long.bitCount(this.board_black);
    int whiteCount = Long.bitCount(this.board_white);
    int blockCount = Long.bitCount(this.board_obstacle);
    int noneCount = LENGTH - blackCount - whiteCount - blockCount;

    return Map.of(
        BLACK, (long) blackCount,
        WHITE, (long) whiteCount,
        BLOCK, (long) blockCount,
        NONE, (long) noneCount
    );
  }

  /**
   * 指定された色の合法手をすべて見つける
   * 
   * @param color プレイヤーの色
   * @return 合法手のリスト（パスを含む場合がある）
   */
  public List<Move> findLegalMoves(Color color) {
    return findLegalIndexes(color).stream()
        .map(k -> new Move(k, color)).toList();
  }

  /**
   * 指定された色の合法手のインデックスを見つける
   * 
   * @param color プレイヤーの色
   * @return 合法手のインデックスのリスト（打てる手がない場合はパスを含む）
   */
  List<Integer> findLegalIndexes(Color color) {
    var moves = findNoPassLegalIndexes(color);
    if (moves.size() == 0)
      moves.add(Move.PASS);
    return moves;
  }

  /**
   * パスを除く合法手のインデックスを見つける
   * 
   * @param color プレイヤーの色
   * @return 実際に石を置ける位置のインデックスのリスト
   */
  List<Integer> findNoPassLegalIndexes(Color color) {
    var moves = new ArrayList<Integer>();
    for (int k = 0; k < LENGTH; k++) {
      var c = this.get(k);
      if (c != NONE)
        continue;
      for (var line : lines(k)) {
        var outflanking = outflanked(line, color);
        if (outflanking.size() > 0)
          moves.add(k);
      }
    }
    return moves;
  }

  /**
   * 指定された位置から8方向の座標リストを取得する
   * 
   * @param k 基準となる位置のインデックス
   * @return 8方向それぞれの座標リスト
   */
  List<List<Integer>> lines(int k) {
    var lines = new ArrayList<List<Integer>>();
    for (int dir = 0; dir < 8; dir++) {
      var line = Move.line(k, dir);
      lines.add(line);
    }
    return lines;
  }

  /**
   * 指定された方向に対して、挟める石のリストを返す
   * 
   * @param line  調べる方向の座標リスト
   * @param color 石の色
   * @return 挟める石の位置とその色のリスト
   */
  List<Move> outflanked(List<Integer> line, Color color) {
    if (line.size() <= 1)
      return new ArrayList<Move>();
    var flippables = new ArrayList<Move>();
    for (int k : line) {
      var c = get(k);
      if (c == NONE || c == BLOCK)
        break;
      if (c == color)
        return flippables;
      flippables.add(new Move(k, color));
    }
    return new ArrayList<Move>();
  }

  /**
   * 指定された手を打った後のボード状態を返す
   * 
   * @param move 打つ手（位置と色）
   * @return 新しいボード状態
   */
  public MyBoard placed(Move move) {
    var b = clone();
    b.move = move;

    if (move.isPass() | move.isNone())
      return b;

    var k = move.getIndex();
    var color = move.getColor();
    var lines = b.lines(k);
    for (var line : lines) {
      for (var p : outflanked(line, color)) {
        b.set(p.getIndex(), color);
      }
    }
    b.set(k, color);

    return b;
  }

  /**
   * 盤面の色を反転したボードを返す
   * 黒と白を入れ替えた新しいボードを作成する
   * 
   * @return 色を反転した新しいボード
   */
  public MyBoard flipped() {
    var b = clone();
    b.board_black = this.board_white;
    b.board_white = this.board_black;
    return b;
  }
}
