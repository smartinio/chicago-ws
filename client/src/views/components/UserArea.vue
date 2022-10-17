<template>
  <Player
    :isMe="true"
    variant="large"
    fallbackName="You"
    :player="controlPlayer"
    :baseMove="baseMove"
    :currentPlayer="currentPlayer"
  />
</template>
<script>
import Player from '@/views/components/Player'
export default {
  name: 'UserArea',
  props: ['controlPlayer', 'baseMove', 'currentPlayer'],
  components: {
    Player
  }
}
</script>
