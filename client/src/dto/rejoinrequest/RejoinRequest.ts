export default class RejoinRequest {
  constructor (public playerId: string, public key: string) {
    this.playerId = playerId
    this.key = key
  }
}
