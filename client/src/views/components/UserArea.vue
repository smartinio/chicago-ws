<template>
  <Player
    :isMe="true"
    variant="large"
    fallbackName="You"
    :player="controlPlayer"
    :baseMove="baseMove"
    :currentPlayer="currentPlayer"
    :dealer="dealer"
  />
</template>
<script>
import Player from '@/views/components/Player'
export default {
  name: 'UserArea',
  props: ['controlPlayer', 'baseMove', 'currentPlayer', 'dealer'],
  components: {
    Player
  }
}
</script>
