package myplayer;

import static ap25.Board.*;
import static ap25.Color.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import ap25.*;

/**
 * 盤面評価を行うクラス。
 * 盤面の各マスに重み付けを行い、全体の評価値を計算する。
 */
class OurEval {
  /**
   * 盤面評価用の重み行列。
   * 各マスの重要度を表す値が格納されている。
   * 角とその周辺が高い値になっている。
   */
  static float[][] M = {
      { 10,  10, 10, 10,  10,  10},
      { 10,  -5,  1,  1,  -5,  10},
      { 10,   1,  1,  1,   1,  10},
      { 10,   1,  1,  1,   1,  10},
      { 10,  -5,  1,  1,  -5,  10},
      { 10,  10, 10, 10,  10,  10},
  };

  /**
   * 盤面の評価値を計算する。
   * ゲームが終了している場合は最終スコアに大きな重みを付ける。
   * そうでない場合は全マスの重み付け合計を返す。
   */
  public float value(Board board) {
    if (board.isEnd()) return 1000000 * board.score();

    return (float) IntStream.range(0, LENGTH)
      .mapToDouble(k -> score(board, k))
      .reduce(Double::sum).orElse(0);
  }

  /**
   * 指定された位置の評価値を計算する。
   * マスの重みと石の色の値を掛け合わせる。
   */
  float score(Board board, int k) {
    return M[k / SIZE][k % SIZE] * board.get(k).getValue();
  }
}

/**
 * 探索用のスレッドクラス。
 * 各スレッドは特定の手の探索を担当する。
 */
class SearchThread implements Runnable {
  private final Board board;
  private final Move move;
  private final int depth;
  private final OurEval eval;
  private final int depthLimit;
  private float result;
  private final CountDownLatch latch;

  public SearchThread(Board board, Move move, int depth, OurEval eval, int depthLimit, CountDownLatch latch) {
    this.board = board;
    this.move = move;
    this.depth = depth;
    this.eval = eval;
    this.depthLimit = depthLimit;
    this.latch = latch;
  }

  @Override
  public void run() {
    try {
      var newBoard = board.placed(move);
      result = minSearch(newBoard, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, depth + 1);
    } finally {
      latch.countDown();
    }
  }

  public float getResult() {
    return result;
  }

  public Move getMove() {
    return move;
  }

  private float minSearch(Board board, float alpha, float beta, int depth) {
    if (isTerminal(board, depth)) return eval.value(board);

    var moves = board.findLegalMoves(WHITE);
    moves = order(moves);

    for (var move: moves) {
      var newBoard = board.placed(move);
      float v = maxSearch(newBoard, alpha, beta, depth + 1);
      beta = Math.min(beta, v);
      if (alpha >= beta) break;
    }

    return beta;
  }

  private float maxSearch(Board board, float alpha, float beta, int depth) {
    if (isTerminal(board, depth)) return eval.value(board);

    var moves = board.findLegalMoves(BLACK);
    moves = order(moves);

    for (var move: moves) {
      var newBoard = board.placed(move);
      float v = minSearch(newBoard, alpha, beta, depth + 1);

      if (v > alpha) {
        alpha = v;
      }

      if (alpha >= beta)
        break;
    }

    return alpha;
  }

  private boolean isTerminal(Board board, int depth) {
    return board.isEnd() || depth > depthLimit;
  }

  private List<Move> order(List<Move> moves) {
    var shuffled = new ArrayList<Move>(moves);
    Collections.shuffle(shuffled);
    return shuffled;
  }
}

/**
 * オセロAIプレイヤーを実装するクラス。
 * マルチスレッドによる並列探索で最適な手を探す。
 */
public class OurPlayer extends ap25.Player {
  /** プレイヤー名 */
  static final String MY_NAME = "OUR24";
  /** 盤面評価オブジェクト */
  OurEval eval;
  /** 探索の深さ制限 */
  int depthLimit;
  /** 現在選択中の手 */
  Move move;
  /** 内部盤面表現 */
  MyBoard board;
  /** スレッド数 */
  private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

  /**
   * 色のみを指定するコンストラクタ。
   * デフォルトのプレイヤー名、評価関数、深さ制限2で初期化する。
   */
  public OurPlayer(Color color) {
    this(MY_NAME, color, new OurEval(), 2);
  }

  /**
   * すべてのパラメータを指定するコンストラクタ。
   */
  public OurPlayer(String name, Color color, OurEval eval, int depthLimit) {
    super(name, color);
    this.eval = eval;
    this.depthLimit = depthLimit;
    this.board = new MyBoard();
  }

  /**
   * 評価関数にデフォルト値を使用するコンストラクタ。
   */
  public OurPlayer(String name, Color color, int depthLimit) {
    this(name, color, new OurEval(), depthLimit);
  }

  /**
   * 内部盤面表現を更新する。
   */
  public void setBoard(Board board) {
    for (var i = 0; i < LENGTH; i++) {
      this.board.set(i, board.get(i));
    }
  }

  /**
   * プレイヤーが黒番（先手）かどうかを判定する。
   */
  boolean isBlack() { return getColor() == BLACK; }

  /**
   * AIの思考ルーチン。次の一手を決定する。
   * パスが必要な場合はパスを返し、そうでない場合は並列ミニマックス探索で最適手を探す。
   */
  public Move think(Board board) {
    this.board = this.board.placed(board.getMove());

    if (this.board.findNoPassLegalIndexes(getColor()).size() == 0) {
      this.move = Move.ofPass(getColor());
    } else {
      var newBoard = isBlack() ? this.board.clone() : this.board.flipped();
      this.move = null;

      var moves = newBoard.findLegalMoves(BLACK);
      moves = order(moves);
      
      if (!moves.isEmpty()) {
        var threads = new ArrayList<SearchThread>();
        var latch = new CountDownLatch(moves.size());
        var bestScore = Float.NEGATIVE_INFINITY;
        Move bestMove = null;

        // 各手に対してスレッドを作成
        for (var move : moves) {
          var thread = new SearchThread(newBoard, move, 0, eval, depthLimit, latch);
          threads.add(thread);
          new Thread(thread).start();
        }

        try {
          // すべてのスレッドの完了を待つ
          latch.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        // 最適な手を選択
        for (var thread : threads) {
          var score = thread.getResult();
          if (score > bestScore) {
            bestScore = score;
            bestMove = thread.getMove();
          }
        }

        this.move = bestMove.colored(getColor());
      }
    }

    this.board = this.board.placed(this.move);
    return this.move;
  }

  /**
   * 手の順序をランダムに並び替える。
   * 同じ評価値の手があった場合に毎回同じ手を選ばないようにするため。
   */
  List<Move> order(List<Move> moves) {
    var shuffled = new ArrayList<Move>(moves);
    Collections.shuffle(shuffled);
    return shuffled;
  }
} 