export default class Message {
  constructor (event) {
    const {type, body} = JSON.parse(event.data)
    this.type = type
    this.body = body
  }
}
