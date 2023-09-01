
import os

def calculate_next_version():

    latest_version = "3.0.1"

    # Split the version string into major, minor, and patch parts
    major, minor, patch = map(int, latest_version.split('.'))

    # Increment the minor version by one
    patch += 1

    next_version = f"{major}.{minor}.{patch}"

    # Set the calculated next version as an environmental variable
    print(next_version)


if __name__ == "__main__":
    calculate_next_version()
