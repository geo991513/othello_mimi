package ap25;

/*
 * playerの抽象クラ
 */
public abstract class Player {
  String name;
  Color color;
  Board board;

  public Player(String name, Color color) {
    this.name = name;
    this.color = color;
  }

  /* ゲームボードを設定する */
  public void setBoard(Board board) { this.board = board; }
  /* 色を取得する */
  public Color getColor() { return this.color; }
  /* nameを返す */
  public String toString() { return this.name; }
  /* プレイヤーの思考を実行し、次の手を決定する */
  public Move think(Board board) { return null; }
}
