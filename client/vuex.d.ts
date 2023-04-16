import { ComponentCustomProperties } from 'vue'
import store from './src/store'

declare module '@vue/runtime-core' {
  export interface ComponentCustomProperties {
    $store: typeof store
  }
}
