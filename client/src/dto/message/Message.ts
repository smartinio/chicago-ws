export default class Message {
  constructor (event: any, public type?: string, public body?: string) {
    const parsed = JSON.parse(event.data)
    this.type = parsed.type
    this.body = parsed.body
  }
}
