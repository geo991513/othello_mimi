package myplayer;

import ap25.*;
import static ap25.Color.*;
import java.util.*;
import java.util.stream.*;

/**
 * オセロゲームのメインクラス
 * ゲームの進行を管理し、プレイヤーの手番制御を行う
 */
public class MyGame {
  public static void main(String args[]) {
    // 黒と白のプレイヤーを初期化
    var player1 = new myplayer.MyPlayer(BLACK);
    var player2 = new myplayer.RandomPlayer(WHITE);
    var board = new MyBoard();
    var game = new MyGame(board, player1, player2);
    game.play();
  }

  // 思考時間の制限（秒）
  static final float TIME_LIMIT_SECONDS = 60;

  // ゲームの状態を保持するフィールド
  Board board;          // ゲームボード
  Player black;         // 黒プレイヤー
  Player white;         // 白プレイヤー
  Map<Color, Player> players;  // 色とプレイヤーの対応付け
  List<Move> moves = new ArrayList<>();  // 手の履歴
  Map<Color, Float> times = new HashMap<>(Map.of(BLACK, 0f, WHITE, 0f));  // 各プレイヤーの思考時間

  /**
   * コンストラクタ
   */
  public MyGame(Board board, Player black, Player white) {
    this.board = board.clone();
    this.black = black;
    this.white = white;
    this.players = Map.of(BLACK, black, WHITE, white);
  }

  /**
   * ゲームを開始し、進行を管理するメソッド
   */
  public void play() {
    // 各プレイヤーにボードの状態を設定
    this.players.values().forEach(p -> p.setBoard(this.board.clone()));

    // ゲームが終了するまでループ
    while (this.board.isEnd() == false) {
      var turn = this.board.getTurn();  // 現在の手番を取得
      var player = this.players.get(turn);  // 手番のプレイヤーを取得

      Error error = null;
      long t0 = System.currentTimeMillis();
      Move move;

      // プレイヤーの手を取得
      try {
        move = player.think(board.clone()).colored(turn);
      } catch (Error e) {
        error = e;
        move = Move.ofError(turn);
      }

      // 思考時間を記録
      long t1 = System.currentTimeMillis();
      final var t = (float) Math.max(t1 - t0, 1) / 1000.f;
      this.times.compute(turn, (k, v) -> v + t);

      // 手の妥当性をチェック
      move = check(turn, move, error);
      moves.add(move);

      // ボードを更新
      if (move.isLegal()) {
        board = board.placed(move);
      } else {
        board.foul(turn);
        break;
      }

      System.out.println(board);
    }

    printResult(board, moves);
  }

  /**
   * 手の妥当性をチェックするメソッド
   */
  Move check(Color turn, Move move, Error error) {
    // エラーチェック
    if (move.isError()) {
      System.err.printf("error: %s %s", turn, error);
      System.err.println(board);
      return move;
    }

    // 時間切れチェック
    if (this.times.get(turn) > TIME_LIMIT_SECONDS) {
      System.err.printf("timeout: %s %.2f", turn, this.times.get(turn));
      System.err.println(board);
      return Move.ofTimeout(turn);
    }

    // 合法手チェック
    var legals = board.findLegalMoves(turn);
    if (move == null || legals.contains(move) == false) {
      System.err.printf("illegal move: %s %s", turn, move);
      System.err.println(board);
      return Move.ofIllegal(turn);
    }

    return move;
  }

  /**
   * 勝者を取得するメソッド
   */
  public Player getWinner(Board board) {
    return this.players.get(board.winner());
  }

  /**
   * ゲーム結果を表示するメソッド
   */
  public void printResult(Board board, List<Move> moves) {
    var result = String.format("%5s%-9s", "", "draw");
    var score = Math.abs(board.score());
    if (score > 0)
      result = String.format("%-4s won by %-2d", getWinner(board), score);

    var s = toString() + " -> " + result + "\t| " + toString(moves);
    System.out.println(s);
  }

  /**
   * プレイヤー情報の文字列表現を返す
   */
  public String toString() {
    return String.format("%4s vs %4s", this.black, this.white);
  }

  /**
   * 手の履歴の文字列表現を返す
   */
  public static String toString(List<Move> moves) {
    return moves.stream().map(x -> x.toString()).collect(Collectors.joining());
  }
}
