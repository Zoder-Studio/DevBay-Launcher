// Warns in the console if the download button link hasn't been set yet,
// so it's obvious during development that it still needs a real URL.
document.addEventListener("DOMContentLoaded", () => {
  const downloadBtn = document.getElementById("download-btn");

  if (downloadBtn && downloadBtn.dataset.placeholder === "true") {
    downloadBtn.addEventListener("click", (event) => {
      event.preventDefault();
      window.location.href = "https://github.com/Zoder-Studio/DevBay-Launcher/releases";
    });

    console.warn(
      "[DevBay site] The #download-btn link is still a placeholder. " +
      "Update its href in index.html once a stable release APK URL is ready, " +
      "and remove the data-placeholder attribute."
    );
  }
});