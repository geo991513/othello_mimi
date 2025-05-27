package ap25;

import java.util.Map;

/**
 * オセロゲームの色を表す列挙型。
 * 各色は数値と文字表現を持つ。
 * BLACK（黒）、WHITE（白）、NONE（空）、BLOCK（障害物）の4つの状態を表現する。
 */
public enum Color {
  /** 黒石を表す (値: 1) */
  BLACK(1),
  /** 白石を表す (値: -1) */
  WHITE(-1),
  /** 空のマスを表す (値: 0) */
  NONE(0),
  /** 障害物のマスを表す (値: 3) */
  BLOCK(3);

  /**
   * 各色の文字表現を定義するマップ。
   * BLACK: "o", WHITE: "x", NONE: " ", BLOCK: "#"
   */
  static Map<Color, String> SYMBOLS =
      Map.of(BLACK, "o", WHITE, "x", NONE, " ", BLOCK, "#");

  /** 色の数値表現 */
  private int value;

  /**
   * Colorのコンストラクタ。
   */
  private Color(int value) {
    this.value = value;
  }

  /**
   * 色の数値表現を取得する。
   * BLACK: 1, WHITE: -1, NONE: 0, BLOCK: 3
   */
  public int getValue() {
    return this.value;
  }

  /**
   * 色を反転させる。
   * BLACK→WHITE、WHITE→BLACKに変換する。
   * NONE、BLOCKは変換されない。
   */
  public Color flipped() {
    switch (this) {
    case BLACK: return WHITE;
    case WHITE: return BLACK;
    default: return this;
    }
  }

  /**
   * 色の文字表現を取得する。
   */
  public String toString() {
    return SYMBOLS.get(this);
  }

  /**
   * 文字列から対応する色を取得する。
   */
  public Color parse(String str) {
    return Map.of("o", BLACK, "x" , WHITE).getOrDefault(str, NONE);
  }
}
