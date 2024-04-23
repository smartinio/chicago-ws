/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2024-04-23 17:55:28.

export interface Action {
    type: ActionType;
    value: string;
}

export interface GameCreation {
    nickname: string;
    rules: GameRules;
}

export interface JoinRequest {
    nickname: string;
    key: string;
}

export interface LeaveRequest {
    playerId: string;
    key: string;
}

export interface Message {
    type: MessageType;
    body: string;
}

export interface RejoinRequest {
    playerId: string;
    key: string;
}

export interface Snapshot {
    me: Player;
    game: ChicagoGame;
    myTurn: boolean;
    imDealing: boolean;
    isOneOpenAvailable: boolean;
    myHand: PlayingCard[];
    myPlayed: PlayingCard[];
}

export interface GameRules {
    chicagoBestHand: boolean;
    chicagoBefore15: boolean;
    numTrades: number;
    tradeBanScore: number;
    oneOpen: OneOpenMode;
    roundWinScore: number;
    winWithTwoScore: number;
}

export interface Player {
    id: string;
    connected: boolean;
    name: string;
    hand: Hand;
    score: number;
    hasTakenChicago: boolean;
    winner: boolean;
}

export interface ChicagoGame {
    host: Player;
    dealer: Player;
    invitationKey: string;
    started: boolean;
    hasWinners: boolean;
    currentRound: Round;
    players: Player[];
    events: GameEvent[];
    oneOpen: OneOpen;
    resetOthersScore: ResetOthersScore;
    rules: GameRules;
}

export interface Hand {
    played: PlayingCard[];
}

export interface Round {
    phase: GamePhase;
    dealer: Player;
    currentPlayer: Player;
    chicagoTaker: Player;
    winner: Player;
    tricks: Trick[];
    isFinalTrade: boolean;
}

export interface OneOpen {
    card: PlayingCard;
    isOpen: boolean;
    player: Player;
}

export interface ResetOthersScore {
    points: number;
    isPending: boolean;
    player: Player;
}

export interface PlayingCard {
    suit: Suit;
    value: Value;
    shortValue: string;
}

export interface GameEvent {
    timestamp: number;
    actor: Player;
    action: EventAction;
    target: Player;
    handType: HandType;
    card: PlayingCard;
    cards: PlayingCard[];
    numCards: number;
    points: number;
    message: string;
    kicked: boolean;
    accepted: boolean;
    reason: string;
    opponentName: string;
}

export interface Trick {
    moves: Move[];
}

export interface Move {
    player: Player;
    card: PlayingCard;
}

export type ActionType = "NEW_GAME" | "JOIN_GAME" | "MOVE" | "CHICAGO" | "THROW" | "THROW_ONE_OPEN" | "RESPOND_ONE_OPEN" | "START_GAME" | "LEAVE_GAME" | "RESTART_GAME" | "PING" | "DEAL_CARDS" | "SEND_CHAT_MESSAGE" | "KICK_PLAYER" | "RECONNECT" | "CHECK_GAME" | "RESPOND_RESET_OTHERS_SCORE";

export type MessageType = "FATAL_ERROR" | "KICKED" | "JSON_ERROR" | "NICKNAME_ERROR" | "KEY_ERROR" | "GAME_ERROR" | "GAME_WINNER" | "SNAPSHOT" | "PONG" | "CURRENTLY_IN_GAME";

export type OneOpenMode = "ALL" | "FINAL";

export type GamePhase = "BEFORE" | "TRADING" | "CHICAGO" | "PLAYING" | "AFTER";

export type Suit = "HEARTS" | "SPADES" | "DIAMONDS" | "CLUBS";

export type Value = "TWO" | "THREE" | "FOUR" | "FIVE" | "SIX" | "SEVEN" | "EIGHT" | "NINE" | "TEN" | "JACK" | "QUEEN" | "KING" | "ACE";

export type EventAction = "CALLED_CHICAGO" | "LOST_CHICAGO" | "PLAYED" | "TRADED" | "WON_BEST_HAND" | "WON_CHICAGO" | "WON_GAME" | "WON_ROUND" | "WON_TRICK" | "WON_ROUND_GUARANTEED" | "CREATED_GAME" | "JOINED_GAME" | "LEFT_GAME" | "BECAME_HOST" | "REQUESTED_ONE_OPEN" | "RESPONDED_TO_ONE_OPEN" | "DECIDING_RESET_OTHERS_SCORE" | "RESPONDED_TO_RESET_OTHERS_SCORE" | "NEW_ROUND" | "TRICK_DONE" | "CHAT_MESSAGE";

export type HandType = "NOTHING" | "PAIR" | "TWO_PAIR" | "THREE_OF_A_KIND" | "STRAIGHT" | "FLUSH" | "FULL_HOUSE" | "FOUR_OF_A_KIND" | "STRAIGHT_FLUSH" | "ROYAL_STRAIGHT_FLUSH";
