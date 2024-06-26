import EmojiConvertor from 'emoji-js'

import { PlayingCard, Player } from '@/server-types'

const suits = {
  CLUBS: '♣️',
  HEARTS: '❤️',
  DIAMONDS: '♦️',
  SPADES: '♠️'
}

const isRed = (card: PlayingCard) => ['HEARTS', 'DIAMONDS'].includes(card.suit)

const capitalize = (word: string) => {
  const [first, ...rest] = word.split('')
  return first.toUpperCase() + rest.join('').toLowerCase()
}

const niceCard = (card: PlayingCard) => {
  const color = isRed(card) ? ' style="color:red"' : ''
  return `<b${color}>${suits[card.suit]} ${card.shortValue}</b>`
}

const emoji = new EmojiConvertor();
emoji.replace_mode = 'img'
emoji.img_set = 'apple'
emoji.img_sets.apple.path = 'https://raw.githubusercontent.com/iamcal/emoji-data/master/img-apple-64/'

const emojify = (string?: string) => string ? emoji.replace_unified(string) : undefined

const withEmojis = (callback: (...args: any[]) => string | undefined) => (...args: any[]) => emojify(callback(...args))

export default {
  methods: {
    getCardUrl (card: Pick<PlayingCard, 'suit' | 'value'>) {
      return `cards/${card.value}_${card.suit}.svg`
    },
    getAvatarUrl (player: Player) {
      return `https://api.dicebear.com/8.x/avataaars-neutral/svg?seed=${player.name}`
    },
    niceCard,
    capitalize,
    withEmojis,
    emojify,
  }
}
