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
  }
}
