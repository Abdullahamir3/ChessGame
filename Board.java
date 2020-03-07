public class Board {
    private Piece[][] board = new Piece[8][8];

    // ------------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------------

    /**
     * Removes all pieces from the board.
     */
    public void clear() {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                board[rank][file] = null;
            }
        }
    }

    /**
     * Sets the back two ranks of each player to be the starting position. The
     * back rank gets the sequence <R, K, B, Q, K, B, K, N>, and the second to
     * last rank gets eight pawns.
     */
    public void initialize() {
        clear();

        for (int file = 0; file < 8; file++) {
            board[1][file] = new Pawn(this, Player.BLACK);
            board[6][file] = new Pawn(this, Player.WHITE);
        }

        board[0][0] = new Rook(this, Player.BLACK);
        board[0][1] = new Knight(this, Player.BLACK);
        board[0][2] = new Bishop(this, Player.BLACK);
        board[0][3] = new Queen(this, Player.BLACK);
        board[0][4] = new King(this, Player.BLACK);
        board[0][5] = new Bishop(this, Player.BLACK);
        board[0][6] = new Knight(this, Player.BLACK);
        board[0][7] = new Rook(this, Player.BLACK);

        board[7][0] = new Rook(this, Player.WHITE);
        board[7][1] = new Knight(this, Player.WHITE);
        board[7][2] = new Bishop(this, Player.WHITE);
        board[7][3] = new Queen(this, Player.WHITE);
        board[7][4] = new King(this, Player.WHITE);
        board[7][5] = new Bishop(this, Player.WHITE);
        board[7][6] = new Knight(this, Player.WHITE);
        board[7][7] = new Rook(this, Player.WHITE);
    }

    // ------------------------------------------------------------------------
    // Status
    // ------------------------------------------------------------------------

    /**
     * Determine if a player's king is threatened. We do this by looping through
     * every opponent piece and checking if the getAllMoves list contains a tile
     * occupied by the player's king.
     *
     * @param player The player
     * @return true if the player's king is threatened, false otherwise
     */
    public boolean isPlayerInCheck(Player player) {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Tile t1 = new Tile(rank, file);

                if (isOccupiedByPlayer(t1, player.opposite())) {
                    for (Tile t2 : getPieceAt(t1).getAllMoves(t1)) {
                        if (isOccupiedByPlayer(t2, player) && getPieceAt(t2).isKing()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determine if a player is in checkmate. A player is in checkmate if
     * all of their pieces have no safe moves.
     *
     * @param player The player.
     * @return true if the player is in checkmate, false otherwise.
     */
    public boolean isPlayerInCheckMate(Player player) {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Tile t1 = new Tile(rank, file);

                if (isOccupiedByPlayer(t1, player)) {
                    if (!getPieceAt(t1).getAllSafeMoves(t1).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // ------------------------------------------------------------------------
    // Position
    // ------------------------------------------------------------------------

    /**
     * Determines if a tile is occupied.
     *
     * @param t The tile.
     * @return true if the tile is occupied, false otherwise.
     */
    public boolean isOccupied(Tile t) {
        return getPieceAt(t) != null;
    }

    /**
     * Determines if a tile is occupied by a particular player.
     *
     * @param t      The tile.
     * @param player The player.
     * @return true if the tile is occupied by player, false otherwise.
     */
    public boolean isOccupiedByPlayer(Tile t, Player player) {
        return getPieceAt(t) != null && getPieceAt(t).getPlayer() == player;
    }

    /**
     * Retrieves the piece at the given tile.
     *
     * @param t The tile.
     * @return The piece at the given tile, or null if the tile is unoccupied.
     */
    public Piece getPieceAt(Tile t) {
        if (!t.isValid()) {
            return null;
        }

        return board[t.getRank()][t.getFile()];
    }

    /**
     * Updates the piece at the given tile.
     *
     * @param t     The tile.
     * @param piece The piece.
     * @throws RuntimeException If the tile is not valid.
     */
    public void setPieceAt(Tile t, Piece piece) {
        if (!t.isValid()) {
            throw new RuntimeException("Tile is not valid.");
        }

        board[t.getRank()][t.getFile()] = piece;
    }

    // ------------------------------------------------------------------------
    // Movement
    // ------------------------------------------------------------------------

    /**
     * Attempts to move a piece from the source tile to the destination tile. This method
     * may fail for the following reasons:
     * <p>
     * <ul>
     * <li>The source or destination tile refers outside the board.</li>
     * <li>The source tile is unoccupied.</li>
     * <li>The piece at the source tile cannot move or capture the destination tile.</li>
     * </ul>
     *
     * @param from The source tile.
     * @param to   The destination tile.
     * @return true if movement succeeded, false otherwise
     */
    public boolean move(Tile from, Tile to) {
        if (!from.isValid() || !to.isValid() || from.equals(to) || !isOccupied(from)) {
            return false;
        }

        Piece piece = getPieceAt(from);

        if (!piece.canMove(from, to) || wouldPutInCheck(from, to)) {
            return false;
        }

        setPieceAt(to, piece);
        setPieceAt(from, null);
        return true;
    }

    /**
     * Determines if moving the piece on the source tile to the destination
     * tile would put the player owning the piece in check.
     *
     * @param from The source tile.
     * @param to   The destination tile.
     * @return true if the move is safe, false otherwise.
     */
    public boolean wouldPutInCheck(Tile from, Tile to) {
        Piece piece = getPieceAt(from);
        Piece replaced = getPieceAt(to);

        setPieceAt(to, piece);
        setPieceAt(from, null);

        boolean inCheck = isPlayerInCheck(piece.getPlayer());

        setPieceAt(to, replaced);
        setPieceAt(from, piece);

        return inCheck;
        
    }
    

    
}
