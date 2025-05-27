package ap25;

import static ap25.Board.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * オセロの手を表すクラス
 * 手の位置、色、特殊な手（パス、タイムアウトなど）を管理
 */
public class Move {
  /** パスを表す特殊なインデックス */
  public final static int PASS = -1;
  /** タイムアウトを表す特殊なインデックス */
  final static int TIMEOUT = -10;
  /** 反則手を表す特殊なインデックス */
  final static int ILLEGAL = -20;
  /** エラーを表す特殊なインデックス */
  final static int ERROR = -30;

  /** 手の位置（0からLENGTH-1、または特殊なインデックス） */
  int index;
  /** 手の色（BLACK, WHITE, NONE） */
  Color color;

  /**
   * インデックスと色から手を作成
   */
  public static Move of(int index, Color color) {
    return new Move(index, color);
  }

  /**
   * 位置文字列（例："a1"）と色から手を作成
   */
  public static Move of(String pos, Color color) {
    return new Move(parseIndex(pos), color);
  }

  /**
   * パスを作成
   */
  public static Move ofPass(Color color) {
    return new Move(PASS, color);
  }

  /**
   * タイムアウトを作成
   */
  public static Move ofTimeout(Color color) {
    return new Move(TIMEOUT, color);
  }

  /**
   * 反則手を作成
   */
  public static Move ofIllegal(Color color) {
    return new Move(ILLEGAL, color);
  }

  /**
   * エラー手を作成
   */
  public static Move ofError(Color color) {
    return new Move(ERROR, color);
  }

  /**
   * コンストラクタ
   */
  public Move(int index, Color color) {
    this.index = index;
    this.color = color;
  }

  /** インデックスを取得 */
  public int getIndex() { return this.index; }
  /** 行を取得（0からSIZE-1） */
  public int getRow() { return this.index / SIZE; }
  /** 列を取得（0からSIZE-1） */
  public int getCol() { return this.index % SIZE; }
  /** 色を取得 */
  public Color getColor() { return this.color; }
  /** ハッシュコードを計算 */
  public int hashCode() { return Objects.hash(this.index, this.color); }

  /**
   * 等価性を判定
   */
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Move other = (Move) obj;
    return this.index == other.index && this.color == other.color;
  }

  /** 色がNONEかどうかを判定 */
  public boolean isNone() { return this.color == Color.NONE; }

  /** 合法手かどうかを判定 */
  public boolean isLegal() { return this.index >= PASS; }
  /** パスかどうかを判定 */
  public boolean isPass() { return this.index == PASS; }

  /** 反則手かどうかを判定 */
  public boolean isFoul() { return this.index < PASS; }
  /** タイムアウトかどうかを判定 */
  public boolean isTimeout() { return this.index == TIMEOUT; }
  /** 反則手かどうかを判定 */
  public boolean isIllega() { return this.index == ILLEGAL; }
  /** エラー手かどうかを判定 */
  public boolean isError() { return this.index == ERROR; }

  /**
   * 色を反転した手を取得
   */
  public Move flipped() {
    return new Move(this.index, this.color.flipped());
  }

  /**
   * 指定された色に変更した手を取得
   */
  public Move colored(Color color) {
    return new Move(this.index, color);
  }

  /**
   * 指定された列と行が盤面内かどうかを判定
   */
  public static boolean isValid(int col, int row) {
    return 0 <= col && col < SIZE && 0 <= row && row < SIZE;
  }

  /**
   * 指定された距離の8方向のオフセットを取得
   * 上、右上、右、右下、下、左下、左、左上の順
   */
  static int[][] offsets(int dist) {
    return new int[][] {
      { -dist, 0 }, { -dist, dist }, { 0, dist }, { dist, dist },
      { dist, 0 }, { dist, -dist }, { 0, -dist }, { -dist, -dist } };
  }

  /**
   * 指定された位置の隣接する8マスの位置を取得
   */
  public static List<Integer> adjacent(int k) {
    var ps = new ArrayList<Integer>();
    int col0 = k % SIZE, row0 = k / SIZE;

    for (var o : offsets(1)) {
      int col = col0 + o[0], row = row0 + o[1];
      if (Move.isValid(col, row)) ps.add(index(col, row));
    }

    return ps;
  }

  /**
   * 指定された位置から指定された方向のマスを取得
   * @param k 開始位置
   * @param dir 方向（0-7、offsetsの順）
   */
  public static List<Integer> line(int k, int dir) {
    var line = new ArrayList<Integer>();
    int col0 = k % SIZE, row0 = k / SIZE;

    for (int dist = 1; dist < SIZE; dist++) {
      var o = offsets(dist)[dir];
      int col = col0 + o[0], row = row0 + o[1];
      if (Move.isValid(col, row) == false)
        break;
      line.add(index(col, row));
    }

    return line;
  }

  /**
   * 列と行からインデックスを計算
   */
  public static int index(int col, int row) {
    return SIZE * row + col;
  }

  /**
   * 手の文字列表現を取得（例："a1", ".."（パス）, "@"（タイムアウト））
   */
  public String toString() {
    return toIndexString(this.index);
  }

  /**
   * 位置文字列（例："a1"）からインデックスを解析
   */
  public static int parseIndex(String pos) {
    return SIZE * (pos.charAt(1) - '1') + pos.charAt(0) - 'a';
  }

  /**
   * インデックスから位置文字列を生成
   */
  public static String toIndexString(int index) {
    if (index == PASS) return "..";
    if (index == TIMEOUT) return "@";
    return toColString(index % SIZE) + toRowString(index / SIZE);
  }

  /**
   * 列番号から列文字（a-h）を生成
   */
  public static String toColString(int col) {
    return Character.toString('a' + col);
  }

  /**
   * 行番号から行文字（1-8）を生成
   */
  public static String toRowString(int row) {
    return Character.toString('1' + row);
  }

  /**
   * インデックスのリストから位置文字列のリストを生成
   */
  public static List<String> toStringList(List<Integer> moves) {
    return moves.stream().map(k -> toIndexString(k)).toList();
  }
}
