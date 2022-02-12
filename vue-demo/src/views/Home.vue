<template>
  <div>
    <Header></Header>
    <h1>Home</h1>
    <Footer></Footer>
  </div>
</template>

<script>

// @ is an alias to /src
import Header from "@/components/Header";
import Footer from "@/components/Footer";

export default {
  name: 'Home',
  components: {
    Header,
    Footer
  }
}
</script>
