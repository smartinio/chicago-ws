export default class JoinRequest {
  constructor (public nickname: string, public key: string) {
    this.nickname = nickname
    this.key = key
  }
}
