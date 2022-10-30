import EmojiConvertor from 'emoji-js'

const suits = {
  CLUBS: '♣️',
  HEARTS: '❤️',
  DIAMONDS: '♦️',
  SPADES: '♠️'
}

const capitalize = (word) => {
  const [first, ...rest] = word.split('')
  return first.toUpperCase() + rest.join('').toLowerCase()
}

const niceCard = (card) => {
  const Card = capitalize(card.value)
  return `${suits[card.suit]} ${Card}`
}

const emoji = new EmojiConvertor();
emoji.replace_mode = 'img'
emoji.img_set = 'apple'
emoji.img_sets.apple.path = 'https://raw.githubusercontent.com/iamcal/emoji-data/master/img-apple-64/'

const withEmojis = (callback) => (...args) => {
  const string = callback(...args)
  return string ? emoji.replace_unified(string) : undefined
}

export default {
  methods: {
    getCardUrl (card) {
      return `static/cards/${card.value}_${card.suit}.svg`
    },
    getAvatarUrl (player) {
      return `https://avatars.dicebear.com/api/adventurer-neutral/${player.id}.svg`
    },
    niceCard,
    capitalize,
    withEmojis,
  }
}
