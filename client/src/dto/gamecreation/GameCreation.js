export default class GameCreation {
  constructor (nickname, rules) {
    this.nickname = nickname
    this.rules = {
      chicagoBestHand: rules.chicagoBestHand,
      numTrades: rules.numTrades,
      oneOpen: rules.oneOpen,
    }
  }
}
