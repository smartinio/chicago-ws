<template>
  <Player
    :isMe="true"
    variant="large"
    fallbackName="You"
    :player="user"
    :baseMove="baseMove"
    :currentPlayer="currentPlayer"
  />
</template>
<script>
import Player from '@/views/components/Player'
export default {
  name: 'UserArea',
  props: ['user', 'baseMove', 'currentPlayer'],
  components: {
    Player
  }
}
</script>
